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
	private LocalDateTime red;
	
	private List<CommentEntity> comments;
	
	private List<detachfile> detachfiles;
	private MemberEntity member;
	
	public NoticeDto(NoticeEntity entity) {
		 num=entity.getId();
		 username=entity.getUsername();
		 nickname=entity.getNickname();
		 title=entity.getTitle();
		 text=entity.getText();
		 red=entity.getRed();
		 comments=entity.getComments();
		 likes=entity.getLikeuser().size();
		 detachfiles=entity.getFiles();
	}
	
}
