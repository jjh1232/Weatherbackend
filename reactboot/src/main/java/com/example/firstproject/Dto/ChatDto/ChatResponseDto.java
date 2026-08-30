package com.example.firstproject.Dto.ChatDto;

import java.time.LocalDateTime;

import com.example.firstproject.Entity.MemberEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChatResponseDto {

	private Long roomid;
	private String messageType;
	private MemberEntity sender;
	
	private String message;
	private String red;
}
