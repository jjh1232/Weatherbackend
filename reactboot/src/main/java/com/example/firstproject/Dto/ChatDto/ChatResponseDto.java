package com.example.firstproject.Dto.ChatDto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChatResponseDto {

	private Long roomId;
	private String messageType;
	
	private String writer;
	private String message;
	private LocalDateTime time;
}
