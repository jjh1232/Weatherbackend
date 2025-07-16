package com.example.firstproject.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.firstproject.Dto.ChatDto.ChatResponseDto;
import com.example.firstproject.Dto.ChatDto.stompchatDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.MeseageDto;
import com.example.firstproject.Service.Followservice.FollowService;
import com.example.firstproject.Service.chatService.ChatService;
import com.example.firstproject.aop.NoLogging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class StompController { 

	private final SimpMessageSendingOperations template;
	
	//db에저장하자
	private final ChatService chatservice;
	
	@Qualifier("redisTemplateString")
	private final RedisTemplate<String, String> redistemplate;
	//이벤트리스너가 실행이안되는데 이유를 모르겠어.. 그냥 채널인터셉터에서핵ㄹ
	@EventListener//이벤트가 밠생할시 실행되는 메서드 이걸로 강한결합을 분리할수있음
	@NoLogging
	public void handlwebsocketconnect(SessionConnectEvent event) {
		System.out.println("컨트롤러 스톰프세션연결시작");
	}
	@EventListener
	@NoLogging
	public void handlertest(SessionConnectedEvent event) {
		System.out.println("커넥티드이벤트");
	}
	//연결해제시 
	@EventListener
	@NoLogging
	public void handlesocketdisco(SessionDisconnectEvent event) {
		/* 왜인지실행이안됨
		System.out.println("디스커넥트시작");
		StompHeaderAccessor accesor=StompHeaderAccessor.wrap(event.getMessage());
		String sessionid=accesor.getSessionId();
		String userkey="stomp:sessiontouser:"+sessionid;
		String userid=(String) redistemplate.opsForValue().get(userkey);
		if(userid !=null) {
			redistemplate.opsForSet().remove("stomp:useridtosession:"+userid, sessionid);
			redistemplate.delete(userkey);//유저키삭제
			if(redistemplate.opsForSet().size("stomp:useridtosession:"+userid)==0) {
				redistemplate.delete("stomp:useridtosession:"+userid);
			}
		}else {
			System.out.println("해당id못찾음:"+sessionid);
		}
		System.out.println("연결종료 id:"+sessionid);
		*/
	}
	//메세지발행
	@NoLogging
	@MessageMapping("/channel/{roomid}") //pub를 붙여 메세지 발행시 들어오는 처리 ex)pub/chat/{userid}
	//@Sendto("주소") //이걸로리턴으로 보낼수도있다고함
	public void sendMessage(@DestinationVariable Long roomid,stompchatDto messageDto ) throws IllegalAccessException { //만든 챗메세지 dto와 @Header등으로 헤더정보나 메세지를 가져옴
		log.info("해당챗방룸아이디 :"+roomid);
		log.info("챗룸데이터:"+messageDto.getMessage());
		
		//@PathVariable ("userid") String userid
		MeseageDto dto=chatservice.chatsave(roomid,messageDto);
		
		
		template.convertAndSend("/sub/channel/"+roomid,dto);//해당하는 토픽에 구독한 주소,그리고 메세지를 전달 
	}
	
	//메세지수신확인
	@NoLogging
	@MessageMapping("/read")    //Json을 Dto로 받기귀찮을때
	public void handlereadmessage(@Payload Map<String,Object> payload,Principal principal) {
		
		 Long roomid = Long.valueOf(payload.get("roomid").toString());
		    Long messageid = Long.valueOf(payload.get("messageid").toString());
		    System.out.println("리턴룸아이디:"+roomid+"리턴챗아이디:"+messageid);
		    System.out.println("로그인유저아이디:"+principal.getName());
		    String userid=principal.getName();
		//유저정보를 커넥트에 저장하거나 아니면 바디에넘어보내는데 후자는 보안이약함
		    String rediskey="stomp:chat:lastread:roomid:"+roomid+":userid:"+userid;
		   
		    redistemplate.opsForValue().set(rediskey, messageid.toString());
	}
	
}
