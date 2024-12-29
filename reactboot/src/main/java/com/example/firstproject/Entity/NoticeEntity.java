package com.example.firstproject.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.firstproject.Dto.NoticeDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="notice")
@EntityListeners(AuditingEntityListener.class)//이거 createDate 핅수
public class NoticeEntity {

	@GeneratedValue(strategy =GenerationType.IDENTITY)
	@Id
	private Long id;
	@Column(nullable= false)
	private String username;
	@Column(nullable= false)
	private String nickname;
	
	@Column(nullable= false, unique=true)
	private String title;
	@Column(nullable= false)
	private String text;
	
	@OneToMany(mappedBy = "notice",fetch = FetchType.LAZY,cascade=CascadeType.ALL)
	@Builder.Default
	private List<detachfile> files=new ArrayList<detachfile>();
	
	@CreatedDate
	@Column(updatable = false)//스프링부트말고 자바컬럼 업데이트시점에서 업데이트막음 
	private LocalDateTime red;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="Member_id")
	@JsonIgnore
	private MemberEntity member;
	
	@OneToMany(mappedBy = "notice",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
	private List<CommentEntity> comments;
	
	public void addcomments(CommentEntity comment) {
		comments.add(comment);
	}
	public void addfiles(detachfile file) {
		
		files.add(file);
	}
	public void removefiles(Long id) {
		
		files.remove(id);
		
	}
	public NoticeDto toDto(Long num,String username,String nickname,String title, String text,LocalDateTime red, List<CommentEntity> comments,List<detachfile> detachfiles) {
		return NoticeDto.builder()
				.num(num)
				.username(username)
				.nickname(nickname)
				.title(title)
				.text(text)
				.red(red)
				.comments(comments)
				.detachfiles(detachfiles)
				.build();
	}
	
	public NoticeDto toDto(Long num,String username,String nickname,String title, String text,LocalDateTime red) {
		return NoticeDto.builder()
				.num(num)
				.username(username)
				.nickname(nickname)
				.title(title)
				.text(text)
				.red(red)
				
				.build();
	}
}
