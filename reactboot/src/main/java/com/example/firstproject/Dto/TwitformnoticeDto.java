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
}
