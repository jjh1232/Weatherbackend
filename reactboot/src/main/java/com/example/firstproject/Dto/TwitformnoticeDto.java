package com.example.firstproject.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TwitformnoticeDto {

	private Long id;
	private String title;
	private String username;
	private String nickname;
	private String userprofile;
	
	private String red;
	private String text;
	private String pty;
	private String rain;
	private String sky;
	private String temp;
	private String reh;
	private String wsd;
	
	private long likes;
	private boolean likely;
	private boolean blockcheck;
	private long views;
	private long commentcount;
	//프로필 URL/표시용 핸들. 이메일(username)을 화면에 노출하지 않으려고 같이 내려준다.
	private String profileid;
}
