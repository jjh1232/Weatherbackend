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
	//[삭제됨] private String password;
	//[삭제됨] private String refreshtoken;
	//이 DTO는 /open/membercreate 응답으로 나간다. 비밀번호 해시와 리프레시토큰이
	//클라이언트로 전달될 이유가 없어서 필드 자체를 두지 않는다.
	//(비밀번호 찾기는 임시비번을 새로 발급하는 방식이라 꺼내 쓸 일도 없다)
	private String profileid;
	private String nickname;

	private String role;

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
		this.profileid=member.getProfileid();
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
