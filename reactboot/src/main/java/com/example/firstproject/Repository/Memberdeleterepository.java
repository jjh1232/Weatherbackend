package com.example.firstproject.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class Memberdeleterepository {
	//서버에 저장할려고 만든리포지토리
	private Map<String, String> memberdeletecode=new ConcurrentHashMap<>(); //멀티스레드환경의맵
	
	
	public void memberdeletecodesave(String username,String authkey) {
		log.info("멤버딜리트코드저장!");
		memberdeletecode.put(username, authkey);
		
	}
	
	public String getdeletecode(String username) {
		log.info("딜리트코드겟");
		String authkey=memberdeletecode.get(username);
		return authkey;
	}
	

}
