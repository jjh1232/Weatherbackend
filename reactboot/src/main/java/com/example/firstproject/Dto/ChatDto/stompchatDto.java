package com.example.firstproject.Dto.ChatDto;

import com.example.firstproject.Dto.ChatDto.Roomdata.EzmemberDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class stompchatDto {

	
	
	private EzmemberDto sender;
	
	private String message;
	
	private String messageType;
}
