package com.example.firstproject.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.firstproject.Service.Memberservice.MemberService;
import com.example.firstproject.Service.Memberservice.SseService;
import com.example.firstproject.configure.PrincipalDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
//@CrossOrigin("*")
public class SSEController {

	//sse를통한 알림메세지를 받은 사용자들 저장할장소 로매바용
	public static Map<Long,SseEmitter> sseEmitters=new ConcurrentHashMap<>();
	private final MemberService memberservice;
	
	private final SseService sseservice;
	//======================SSE알림 서비스=======================
	
	@GetMapping("/ssesub")
	public SseEmitter sse(Authentication authentication) {
		//
		PrincipalDetails cipal=(PrincipalDetails) authentication.getPrincipal();
		
		Long userid=cipal.getMember().getId();
		System.out.println("어센티케이션유저아이디:"+userid);
		
		return sseservice.SSEcon(userid);
	}
	@GetMapping("/ssetest")
	public SseEmitter sse(Long id) {
		//
		
		
	
		System.out.println("어센티케이션유저아이디:"+id);
		
		return sseservice.SSEcon(id);
	}
	

	
	
}
