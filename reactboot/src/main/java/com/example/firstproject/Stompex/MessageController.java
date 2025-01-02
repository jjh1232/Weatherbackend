package com.example.firstproject.Stompex;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MessageController {

	//두개다 비슷한거같은데 ..
	private final SimpMessageSendingOperations sendingoperations;
	private final SimpMessagingTemplate template;
	
	@MessageMapping("/chat/message")
	public void enter(ChatMessage message) {
		if (message.getType().equals(ChatMessage.MessageType.ENTER)) {
			message.setSender("시스템메세지");
			message.setMessage(message.getSender()+"님이입장하였습니다");
		}
		sendingoperations.convertAndSend("/sub/chat/room/"+message.getRoomId(),message);
	}


	@MessageMapping("/simplestomp")
	public void sendmessage(ChatMessage message) {
		template.convertAndSend("/sub/chat/"+message.getRoomId(),message);
	}
}
