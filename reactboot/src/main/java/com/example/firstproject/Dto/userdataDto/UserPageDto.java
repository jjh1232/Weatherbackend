package com.example.firstproject.Dto.userdataDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPageDto {

	private Long userid;
	
	private String username;
	
	private String nickname;
	
	private String myintro;
	
	private String profileimg;
	
	private String regdate;
	
	private long follownum;
	private long followernum;
	
	private boolean followcheck;
	
}
