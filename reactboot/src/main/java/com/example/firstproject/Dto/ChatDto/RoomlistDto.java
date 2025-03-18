package com.example.firstproject.Dto.ChatDto;

import java.util.List;
import java.util.Set;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Entity.StompRoom.Room;
import com.example.firstproject.Entity.StompRoom.chatmessage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomlistDto {

	public RoomlistDto(Room room, List<MemberDto> list, String roomname2,chatmessage lastchat,int chatsize) {
		// TODO Auto-generated constructor stub
		this.roomid=room.getId();
		this.roomname=roomname2;
		this.memberlist=list;
		this.time=room.getCreatedDate();
		
		this.red=room.getCreatedDate();
		
		this.chatnum=chatsize;
		this.latelychat=lastchat.getMessage();
		this.lastchatred=lastchat.getCreatedDate();
		
	}

	
	private Long roomid;
	
	private String roomname;
	
	private List<MemberDto> memberlist;
	
	private String time;
	
	private String latelychat;
	
	private int chatnum;
	
	private String lastchatred;
	
	private String red;
}
	
	
	

