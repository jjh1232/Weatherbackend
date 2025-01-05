package com.example.firstproject.Dto;

import java.time.LocalDateTime;

import com.example.firstproject.Entity.Address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class MemberDto {

	private Long id;
	private String username;
	private String password;
	
	private String nickname;
	private String role;
	private String refreshtoken;
	
	private String provider;
	private String providerid;
	private Address homeaddress;
	private String red;
	private String updatered;
	
	
}
