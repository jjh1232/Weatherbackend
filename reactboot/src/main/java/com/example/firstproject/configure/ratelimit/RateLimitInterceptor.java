package com.example.firstproject.configure.ratelimit;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;
import com.example.firstproject.tools.ClientIp;

/**
 * 공개(open) 조회 API 요청 제한.
 *
 * 왜 필요한가:
 *  중복검사 API 는 로그인 없이 "이 이메일이 가입돼 있는가"를 알려준다.
 *  누가 이메일 목록을 자동으로 넣어보면 회원 명단을 통째로 수집할 수 있다(계정 열거).
 *  사람이 손으로 누르는 횟수는 뻔하므로, 그 이상은 막는다.
 *
 * 방식: 고정 창(fixed window) 카운터
 *  - 키는 (규칙 + 클라이언트 IP).
 *  - 창이 열린 지 windowms 가 지나면 카운트를 0으로 되돌리고 새 창을 연다.
 *  - 창 안에서 limit 을 넘으면 429 를 돌려준다.
 *  슬라이딩 윈도우보다 정확도는 떨어지지만(창 경계에서 최대 2배까지 통과),
 *  메모리가 IP 당 숫자 두 개뿐이라 이 규모에는 이쪽이 맞다.
 *
 * 주의: 서버가 여러 대로 늘어나면 이 카운터는 서버마다 따로 센다.
 *       그때는 Redis(이 프로젝트에 이미 있다)로 옮겨야 한다.
 */
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

	/** 경로 하나에 대한 규칙 */
	public static class Rule {
		final String name;
		final int limit;
		final long windowms;

		public Rule(String name, int limit, long windowms) {
			this.name = name;
			this.limit = limit;
			this.windowms = windowms;
		}
	}

	/** IP 하나가 지금 열어둔 창 */
	private static class Window {
		long start;
		int count;

		Window(long start) {
			this.start = start;
		}
	}

	//경로 앞부분 -> 규칙. 위에서부터 먼저 걸리는 것을 쓴다.
	private static final Map<String, Rule> RULES = new LinkedHashMap<>();
	static {
		//중복검사: 사람이 눌러야 하는 버튼이다. 분당 20번이면 충분히 넉넉하다.
		RULES.put("/open/emailcheck", new Rule("emailcheck", 20, 60_000L));
		RULES.put("/open/profileidcheck", new Rule("profileidcheck", 20, 60_000L));
		//아이디찾기: 가입 방식까지 알려주므로 더 조인다.
		RULES.put("/open/usernamefind", new Rule("usernamefind", 10, 60_000L));
		//비밀번호 찾기: 계정 존재 여부를 알려주는 데다 메일까지 나간다. 제일 세게 조인다.
		RULES.put("/open/passwordfind", new Rule("passwordfind", 3, 600_000L));
		//인증메일 재발송: 메일이 실제로 나가는 동작이라 세게 막는다(서비스 안에 쿨다운도 있다).
		RULES.put("/open/member/resend", new Rule("resendverify", 3, 600_000L));
		//가입 요청: 10분에 5번이면 정상 사용자는 걸릴 일이 없다.
		RULES.put("/open/membercreate", new Rule("membercreate", 5, 600_000L));
	}

	private final ConcurrentHashMap<String, Window> counters = new ConcurrentHashMap<>();
	private final AtomicLong lastsweep = new AtomicLong(System.currentTimeMillis());
	private static final long SWEEPINTERVALMS = 300_000L; //5분마다 한 번 청소

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		//CORS 사전 요청(OPTIONS)은 사용자의 행동이 아니다. 세지 않는다.
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}

		Rule rule = findrule(request.getRequestURI());
		if (rule == null) {
			return true;
		}

		long now = System.currentTimeMillis();
		String key = rule.name + "|" + clientip(request);
		Window window = counters.computeIfAbsent(key, k -> new Window(now));

		boolean blocked;
		long retryafter;

		//창 하나에 대한 읽기-수정-쓰기라 원자적으로 처리해야 한다.
		//창 객체별로만 잠그므로 다른 IP 끼리는 서로 기다리지 않는다.
		synchronized (window) {
			if (now - window.start >= rule.windowms) {
				window.start = now;
				window.count = 0;
			}
			window.count++;
			blocked = window.count > rule.limit;
			retryafter = (rule.windowms - (now - window.start) + 999) / 1000;
		}

		maybesweep(now);

		if (blocked) {
			log.warn("요청제한 초과 rule={} key={} ", rule.name, key);
			reject(response, retryafter);
			return false;
		}
		return true;
	}

	private Rule findrule(String uri) {
		if (uri == null) {
			return null;
		}
		for (Map.Entry<String, Rule> e : RULES.entrySet()) {
			if (uri.startsWith(e.getKey())) {
				return e.getValue();
			}
		}
		return null;
	}

	/**
	 * 실제 사용자의 IP 를 찾는다.
	 * 판별 로직은 로그인 이력·요청 로그와 같은 것을 쓴다(tools/ClientIp).
	 */
	private String clientip(HttpServletRequest request) {
		return ClientIp.resolve(request);
	}

	/**
	 * 다 쓴 창을 치운다.
	 * 없으면 IP 를 바꿔가며 요청할 때 맵이 계속 커진다(메모리 누수).
	 */
	private void maybesweep(long now) {
		long last = lastsweep.get();
		if (now - last < SWEEPINTERVALMS) {
			return;
		}
		//여러 요청이 동시에 들어와도 청소는 하나만 한다.
		if (!lastsweep.compareAndSet(last, now)) {
			return;
		}
		counters.entrySet().removeIf(e -> {
			Window w = e.getValue();
			synchronized (w) {
				//가장 긴 창(10분)의 두 배가 지났으면 더 볼 필요가 없다.
				return now - w.start > 1_200_000L;
			}
		});
	}

	private void reject(HttpServletResponse response, long retryafter) throws IOException {
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); //429
		response.setHeader("Retry-After", String.valueOf(retryafter));
		response.setContentType("application/json;charset=UTF-8");
		//프론트의 messageFromError 가 data.message 를 그대로 읽어 토스트에 띄운다.
		response.getWriter().write(
				"{\"message\":\"요청이 너무 잦습니다. " + retryafter + "초 후에 다시 시도해주세요.\"}");
	}
}
