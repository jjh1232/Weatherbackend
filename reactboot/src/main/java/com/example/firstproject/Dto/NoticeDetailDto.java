package com.example.firstproject.Dto;

import java.util.List;

import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Entity.detachfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDetailDto {
	private long num;
	private String username;
	private String nickname;
	private String title;
	private String text;
	private int likes;
	private String temp;
	private String sky;
	private String pty;
	private String rain;
	private String reh;
	private String wsd;
	private boolean likeusercheck;
	private String red;
	private String userprofile;
	
	private List<detachfile> detachfiles;
	private boolean isblock;
}
