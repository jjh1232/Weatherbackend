package com.example.firstproject.Dto.Comment;

import java.time.LocalDateTime;

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
public class Testcom {

	private Long id;
	private Long noticenum;
	private int depth;
	private int cnum;
	private String username;
	private String nickname;
	private String text;
	private String redtime;
	private String userprofile;

	
	
}
