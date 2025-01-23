package com.example.firstproject.Dto.userdataDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginHistoryDto {

	private String username;
	
	private String userlocale;
	
	private String logintime;
	
	private String userip;
	
}
