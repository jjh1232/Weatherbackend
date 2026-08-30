package com.example.firstproject.tools;

import javax.servlet.http.HttpServletRequest;

/**
 * 프록시 뒤에서 실제 사용자 IP 를 찾는다.
 *
 * <p>클라우드플레어(터널 포함)를 거치면 {@code request.getRemoteAddr()} 는 클라우드플레어의
 * IP 라서 <b>모든 사용자가 같은 IP 로 보인다.</b> 그대로 두면
 * <ul>
 *   <li>요청 제한 — 한 사람 때문에 전체가 막힌다</li>
 *   <li>로그인 이력 — 전부 같은 IP 로 기록돼 이력이 무의미해진다</li>
 * </ul>
 *
 * <p>예전엔 이 로직이 세 군데(RateLimitInterceptor · HistoryService · LoggingAspect)에
 * 따로 있었고 서로 달랐다. 요청 제한만 {@code CF-Connecting-IP} 를 보고,
 * 나머지 둘은 {@code X-Forwarded-For} 만 봤다. 한 곳으로 모은다.
 *
 * <p>헤더는 클라이언트가 위조할 수 있다. 신뢰할 수 있는 프록시(클라우드플레어) 뒤에
 * 두고, 서버 포트를 외부에 직접 열지 않는 것이 전제다.
 */
public final class ClientIp {

	private ClientIp() {}

	public static String resolve(HttpServletRequest request) {
		if (request == null) return "unknown";

		//클라우드플레어가 붙여주는 원 IP. 가장 정확하다.
		String ip = request.getHeader("CF-Connecting-IP");

		if (isblank(ip)) {
			//"실제IP, 프록시1, 프록시2" 형태라 맨 앞만 쓴다.
			ip = first(request.getHeader("X-Forwarded-For"));
		}
		if (isblank(ip)) ip = request.getHeader("X-Real-IP");
		if (isblank(ip)) ip = request.getHeader("Proxy-Client-IP");
		if (isblank(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
		if (isblank(ip)) ip = request.getRemoteAddr();

		return isblank(ip) ? "unknown" : ip;
	}

	private static String first(String value) {
		if (isblank(value)) return value;
		int comma = value.indexOf(',');
		return comma < 0 ? value.trim() : value.substring(0, comma).trim();
	}

	private static boolean isblank(String v) {
		return v == null || v.trim().isEmpty() || "unknown".equalsIgnoreCase(v.trim());
	}
}
