package com.example.firstproject.Dto.ChatDto;

import java.util.List;
import java.util.Set;

import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Entity.StompRoom.chatmessage;
import com.example.firstproject.Stompex.ChatMessage;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdminroomdetailDto {

	private Long roomid;
	
	private String roomname;
	
	private Set<MemberRoom> namelist;
	
	
	private List<chatmessage> beforechat;
	

	private String time;
	
	private String lastchatred;
	
	private String red;
	
}
