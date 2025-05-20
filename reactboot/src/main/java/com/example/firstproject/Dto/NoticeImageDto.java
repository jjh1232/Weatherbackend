package com.example.firstproject.Dto;

import com.example.firstproject.Entity.detachfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NoticeImageDto {

	private Long id;
	private String title;
	private String username;
	private String nickname;
	private String userprofile;
	private String mainimage;	
	private String red;
	private long imagenum;
}
