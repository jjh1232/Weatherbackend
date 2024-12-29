package com.example.firstproject.Handler.Websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.firstproject.configure.PrincipalDetails;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChatHandler extends TextWebSocketHandler{//추상클래스 텍스트만필요해서이거고 또 웹소캣핸들러있음
	
	
	//리스트에 접속하고있는모든 세션을 담을것 (유저들관리) map도가능
	//만약 여러개받고싶으면 섲렁페이지에서 매개변수로 받아서 관리하면 되긴함
	private static List<WebSocketSession> list = new ArrayList<>();

	//Map<String,WebSocketSession> loginuser=new HashMap<>();
	
	private String getnickname(WebSocketSession session,Authentication authentication) {
		Map<String,Object> httpSession=session.getAttributes();
		PrincipalDetails principal=(PrincipalDetails) authentication.getPrincipal();
		System.out.println(principal.getUsername());
		return principal.getMember().getNickname();
	}
	
	//소켓에매세지를보냈을떄실행됨 
	protected void handleTextMessage(WebSocketSession session,TextMessage message) throws IOException {
		
		
		System.out.println("메세지수신");
		System.out.println(message);
		String payload=message.getPayload();
		System.out.println("session:"+session.getPrincipal());
		log.info("payload:"+payload);
		String senderId= session.getId();//모든세션은아이디가있음
		//String usernickname=getnickname(session, authentication);
		System.out.println("senderid:"+senderId);
		//System.out.println("usernickname:"+usernickname);
		for(WebSocketSession sess:list) {//세션에있는모든유저에게 메세지를보낸다
			sess.sendMessage(new TextMessage(senderId+":"+message.getPayload()));//메세지보내는거
		}
	}
	
	
	/* Client가 접속 시 호출되는 메서드 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// TODO Auto-generated method stub
		
		System.out.println("클라이언트접속");
        list.add(session);//유저추가

        log.info(session + " 클라이언트 접속");
	}
	
	
	 /* Client가 접속 해제 시 호출되는 메서드드 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		// TODO Auto-generated method stub
		log.info(session + " 클라이언트 접속 해제");
        list.remove(session);
	}
}
