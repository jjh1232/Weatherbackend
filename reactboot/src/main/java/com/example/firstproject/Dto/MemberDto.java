package com.example.firstproject.Dto;

import java.time.LocalDateTime;

import com.example.firstproject.Entity.Address;
import com.example.firstproject.Entity.MemberEntity;

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
	private int usercomments;
	
	private int usernotice;
	
	private int userchatroom;
	
	private String red;
	private String updatered;
	private String profileimg;
	//채팅리스트용생성자
	public MemberDto(MemberEntity member) {
		
		this.id = member.getId();
		this.username = member.getUsername();
		
		this.nickname = member.getNickname();
		this.role = member.getRole();
		this.profileimg=member.getProfileimg();
		/*
		this.refreshtoken = refreshtoken;
		this.provider = provider;
		this.providerid = providerid;
		this.homeaddress = homeaddress;
		this.usercomments = usercomments;
		this.usernotice = usernotice;
		this.userchatroom = userchatroom;
		this.red = red;
		this.updatered = updatered;
		*/
	}
	
	
}
