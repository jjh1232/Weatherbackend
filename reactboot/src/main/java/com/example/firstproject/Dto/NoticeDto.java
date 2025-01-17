package com.example.firstproject.Dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.detachfile;

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
public class NoticeDto {

	
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
	private boolean likeusercheck;
	private String red;
	private String userprofile;
	
	private List<CommentEntity> comments;
	
	private List<detachfile> detachfiles;
	
	
	public NoticeDto(NoticeEntity entity) {
		 num=entity.getNoticeid();
		 username=entity.getNoticeuser();
		 nickname=entity.getNoticenick();
		 title=entity.getTitle();
		 text=entity.getText();
		 red=entity.getRed();
		 comments=entity.getComments();
		 likes=entity.getLikeuser().size();
		 temp=entity.getTemp();
		 sky=entity.getSky();
		 pty=entity.getPty();
		 rain=entity.getRain();
		 userprofile=entity.getMember().getProfileimg();
		 
		 detachfiles=entity.getFiles();
	}
	
}
