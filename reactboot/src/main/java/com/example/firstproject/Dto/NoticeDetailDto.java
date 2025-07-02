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
	private long id;
	private String username;
	private String nickname;
	private String title;
	private String text;
	private String temp;
	private String sky;
	private String pty;
	private String rain;
	private String reh;
	private String wsd;
	private String red;
	private String userprofile;
	
	private List<detachfile> detachfiles;
	private boolean isblock =false;
	private int likes;
	private boolean likeusercheck;
	private long views;
	//생성자를 따로만들어야한다함
	public NoticeDetailDto(
		    long id,
		    String username,
		    String nickname,
		    String title,
		    String text,
		    String temp,
		    String sky,
		    String pty,
		    String rain,
		    String reh,
		    String wsd,
		    String red,
		    String userprofile,
		    long views
		) {
		    this.id = id;
		    this.username = username;
		    this.nickname = nickname;
		    this.title = title;
		    this.text = text;
		    this.temp = temp;
		    this.sky = sky;
		    this.pty = pty;
		    this.rain = rain;
		    this.reh = reh;
		    this.wsd = wsd;
		    this.red = red;
		    this.userprofile = userprofile;
		}
}
