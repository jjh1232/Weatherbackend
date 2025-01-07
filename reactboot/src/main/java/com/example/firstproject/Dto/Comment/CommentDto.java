package com.example.firstproject.Dto.Comment;

import java.time.LocalDateTime;

import com.example.firstproject.Entity.CommentEntity;

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
public class CommentDto {

	private Long id;
	private Long noticenum;
	private int depth;
	private int cnum;
	private String username;
	private String nickname;
	private String text;
	private String redtime;
	private String userprofile;
	
	public CommentEntity toEntity(Long id,int depth,int cnum,String username,String name,
			String text) {
		return CommentEntity.builder()
				.id(id)
				
				.depth(depth)
				.cnum(cnum)
				.username(username)
				.nickname(nickname)
				.text(text)
				
				
				.build();
	}
	
	
	
}
