package com.example.firstproject.controller;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.firstproject.Dto.ChatDto.ChatResponseDto;
import com.example.firstproject.Dto.ChatDto.stompchatDto;
import com.example.firstproject.Service.Followservice.FollowService;
import com.example.firstproject.Service.chatService.ChatService;
import com.example.firstproject.aop.NoLogging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j

public class StompController { 

	private final SimpMessageSendingOperations template;
	
	//db에저장하자
	private final ChatService chatservice;
	
	@EventListener//이벤트가 밠생할시 실행되는 메서드 이걸로 강한결합을 분리할수있음
	public void handlwebsocketconnect(SessionConnectEvent event) {
		System.out.println("세션생성");
	}
	@EventListener
	public void handlesocketdisco(SessionDisconnectEvent event) {
		StompHeaderAccessor accesor=StompHeaderAccessor.wrap(event.getMessage());
		System.out.println("연결종료");
	}
	//메세지발행
	@NoLogging
	@MessageMapping("/channel/{roomid}") //pub를 붙여 메세지 발행시 들어오는 처리 ex)pub/chat/{userid}
	//@Sendto("주소") //이걸로리턴으로 보낼수도있다고함
	public void sendMessage(@DestinationVariable Long roomid,stompchatDto messageDto ) { //만든 챗메세지 dto와 @Header등으로 헤더정보나 메세지를 가져옴
		log.info("해당챗방룸아이디 :"+roomid);
		log.info("메세지발행Dto:"+messageDto.getMessage());
		log.info("메세지발행Dto:"+messageDto.getSender());
		
		//@PathVariable ("userid") String userid
		ChatResponseDto dto=chatservice.chatsave(roomid,messageDto.getSender(),messageDto.getMessageType(),messageDto.getMessage());
		
		
		template.convertAndSend("/sub/channel/"+roomid,dto);//해당하는 토픽에 구독한 주소,그리고 메세지를 전달 
	}
	
	
	
}
