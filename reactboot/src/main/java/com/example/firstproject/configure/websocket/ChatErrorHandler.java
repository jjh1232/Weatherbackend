package com.example.firstproject.configure.websocket;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChatErrorHandler extends StompSubProtocolErrorHandler{

	//에러핸들러 
	//예외가 발생할시 여기에 에러핸들링 
	@Override
	public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
		// TODO Auto-generated method stub
		log.info("스톰프에러");
		return super.handleClientMessageProcessingError(clientMessage, ex);
	}
	
	//필요한 핸들러 만들어서 넘기자 
}
