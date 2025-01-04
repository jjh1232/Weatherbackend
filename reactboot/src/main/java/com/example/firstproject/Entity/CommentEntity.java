package com.example.firstproject.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.firstproject.Dto.Comment.CommentDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="comment")
@EntityListeners(AuditingEntityListener.class)//이거 createDate 핅수
public class CommentEntity {
	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
	
	@Column(nullable = false)
	private int depth;
	
	
	
	@Column
	private int cnum;
	
	@Column(nullable = false)
	private String username;
	
	@Column(nullable = false)
	private String nickname;
	
	@Column(nullable = false)
	private String text;
	
	@CreatedDate
	@Column(nullable = false)
	private LocalDateTime redtime;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="notice_id" )
	@JsonIgnore
	private NoticeEntity notice;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="member_id")
	@JsonIgnore
	private MemberEntity member;
	
	
	
	public CommentDto toDto(Long id,int depth,int cnum,String username,String nickname,
			String text,LocalDateTime redtime,String userprofile) {
		return CommentDto.builder()
				.id(id)
			
				.depth(depth)
				.cnum(cnum)
				.username(username)
				.nickname(nickname)
				.text(text)
				.redtime(redtime)
				.userprofile(userprofile)
				.build();
	}

	
	
	
	

}
