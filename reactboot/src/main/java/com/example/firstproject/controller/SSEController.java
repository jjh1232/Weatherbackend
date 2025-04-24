package com.example.firstproject.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.firstproject.Dto.userdataDto.NotifiResult;
import com.example.firstproject.Dto.userdataDto.NotificationDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Repository.EmitterRepository;
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
	
	private final EmitterRepository emitterrepo;
	
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
	//노티피케이션도 에미터에서하자
	@GetMapping("/notification")
	public ResponseEntity usernotifi(Authentication authentication,@RequestParam(name = "page", defaultValue = "1") int page){
PrincipalDetails cipal=(PrincipalDetails) authentication.getPrincipal();
		
		MemberEntity member=cipal.getMember();
		System.out.println("어디가문제임");
		NotifiResult<NotificationDto> notilist=sseservice.getusernotifi(member.getId(), page);
		System.out.println("어디가문제임1");
		return ResponseEntity.ok(notilist);
		
		
	}
	
	@GetMapping("/notificationcount")
	public ResponseEntity notificount(Authentication authentication) {
PrincipalDetails cipal=(PrincipalDetails) authentication.getPrincipal();
		
		MemberEntity member=cipal.getMember();
		
		Long count=sseservice.notificationcount(member.getId());
		
		return ResponseEntity.ok(count);
	}
	@GetMapping("/ssetest")
	public void sse(Long id) {
		//
		
		//알림보내기
		
		System.out.println("어센티케이션유저아이디:"+id);
		SseEmitter emitter=emitterrepo.get((long) 1).get();
		try {
			emitter.send(emitter.event().id("test").name("message").data("테스트메세지"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		//return sseservice.SSEcon(id);
	}
	
	@GetMapping("/ssetest2")
	public void ssete() {
		
		emitterrepo.getemitteruser();
	}
	
	
}
