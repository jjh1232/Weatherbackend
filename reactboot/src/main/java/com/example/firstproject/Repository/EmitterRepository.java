package com.example.firstproject.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Repository
public class EmitterRepository {

	//유저 id를 키로 SSEemitter를 해시맵에 저장할수있도록구현함
	private Map<String,SseEmitter> emittermap=new ConcurrentHashMap<>();
	
	public SseEmitter save(Long username,SseEmitter sseemitter) {
		//저장
		emittermap.put(getKey(username), sseemitter);
		System.out.println("sse에미터세이브"+username);
		return sseemitter;
	}

	//숫자카운트
	public void getemitteruser() {
		System.out.println("현재에미터유저수:"+emittermap.size());
		
	}
	//모든에미터의 키목록반환
	public void getAllEmiter(){
		System.out.println("키목록반환:"+emittermap.keySet());
	}
	public Optional<SseEmitter> get(Long username){
		
		System.out.println("sseemitter정보:"+username);
		return Optional.ofNullable(emittermap.get(getKey(username)));
	}
	
	public void delete(Long userid) {
		emittermap.remove(getKey(userid));
		System.out.println("에미터맵삭제"+userid);
	}
	
	public SseEmitter exget(Long username) {
		System.out.println("연습용 emitter겟--");
		
		return emittermap.get(getKey(username));
		
	}
	
	private String getKey(Long username) {
		return "emitter:uid"+username;
	}
	
}
