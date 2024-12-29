package com.example.firstproject.Stompex;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {

	//이넘타입으로 송신자와 수신자 채팅방번호 메세지등을담고있는클래스 
	public enum MessageType{
		ENTER,TALK,ALERT
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	private Long id;
	
	@Column(name="type")
	private MessageType type;
	
	@Column(name="room_id")
	private Long roomId;
	
	@Column(name="sender")
	private String sender;
	
	@Column(name="message")
	private String message;
	
	@Column(name="nickname")
	private String nickname;
	
	@Column
	private String profileImg_url;
}
