package com.example.firstproject.Dto.userdataDto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDto {

	private String username;
	
	private String nickname;
	
	private String myintro;
	
	private String profileimg;
	
	private LocalDateTime regdate;
	
	
}
