package com.example.firstproject.Dto.ChatDto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomDto {

	private List<String> usernickname;
	
	private List<String> memberlist;
}
