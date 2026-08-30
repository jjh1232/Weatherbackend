package com.example.firstproject.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

/**
 * SSE 연결(SseEmitter) 보관소.
 *
 * <p><b>왜 유저당 여러 개인가</b><br>
 * 예전에는 키가 유저 id 하나라 <code>put</code> 할 때마다 이전 연결을 덮어썼다.
 * 그래서 탭을 두 개 열면 먼저 연 탭은 연결이 살아 있는데도 알림을 못 받았고,
 * 덮인 emitter 는 onCompletion/onTimeout 이 불리지 않아 맵에 그대로 남았다.
 * 이제 키를 <code>{userid}_{생성시각}</code> 으로 두어 연결마다 한 칸씩 차지하고,
 * 보낼 때는 그 유저의 연결을 전부 순회한다.
 *
 * <p>서버가 여러 대가 되면 이 맵은 서버마다 따로 존재한다.
 * 그때는 Redis pub/sub 으로 옮겨야 한다.
 */
@Repository
@Slf4j
public class EmitterRepository {

	private final Map<String, SseEmitter> emittermap = new ConcurrentHashMap<>();

	/** 키 규칙: userid + "_" + 생성시각(ms). 앞부분으로 유저를 찾는다. */
	public String makekey(Long userid) {
		return userid + "_" + System.currentTimeMillis();
	}

	public SseEmitter save(String key, SseEmitter sseemitter) {
		emittermap.put(key, sseemitter);
		log.info("SSE 연결 저장 key={} (현재 {}개)", key, emittermap.size());
		return sseemitter;
	}

	/** 이 유저의 모든 연결. 탭을 여러 개 열었으면 여러 개가 나온다. */
	public Map<String, SseEmitter> findallbyuserid(Long userid) {
		String prefix = userid + "_";
		Map<String, SseEmitter> found = new ConcurrentHashMap<>();
		emittermap.forEach((key, emitter) -> {
			if (key.startsWith(prefix)) {
				found.put(key, emitter);
			}
		});
		return found;
	}

	/** 전체 연결. 하트비트가 순회할 때 쓴다. */
	public Map<String, SseEmitter> findall() {
		return emittermap;
	}

	public void deletebykey(String key) {
		if (emittermap.remove(key) != null) {
			log.info("SSE 연결 제거 key={} (남은 {}개)", key, emittermap.size());
		}
	}

	/** 로그아웃처럼 이 유저의 연결을 통째로 끊을 때. */
	public void deleteallbyuserid(Long userid) {
		findallbyuserid(userid).forEach((key, emitter) -> {
			//서버 맵에서만 지우면 브라우저는 연결이 살아 있다고 믿고 계속 붙어 있는다.
			//complete() 를 불러야 클라이언트 쪽 EventSource 도 정리된다.
			try {
				emitter.complete();
			} catch (Exception ignore) {
				//이미 닫힌 연결이면 무시한다.
			}
			deletebykey(key);
		});
	}

	public int count() {
		return emittermap.size();
	}

	/** 디버그용 — /emittercheck 에서 호출한다. */
	public void getemitteruser() {
		log.info("현재 SSE 연결 수: {}", emittermap.size());
	}

	public void getAllEmiter() {
		log.info("연결 키 목록: {}", emittermap.keySet());
	}
}
