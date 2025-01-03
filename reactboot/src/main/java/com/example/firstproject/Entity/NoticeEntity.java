package com.example.firstproject.Entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.example.firstproject.Dto.NoticeDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name="notice") //엔티티매핑해야jpql이인식하는듯
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="notice") //이게테이블매핑
@EntityListeners(AuditingEntityListener.class)//이거 createDate 핅수
public class NoticeEntity {

	@GeneratedValue(strategy =GenerationType.IDENTITY)
	@Id
	@Column(nullable= true,name="id")
	private Long noticeid;
	@Column(nullable= false)
	private String noticeuser;
	@Column(nullable= false)
	private String noticenick;
	
	@Column(nullable= false, unique=true)
	private String title;
	@Column(nullable= false)
	private String text;
	//날씨
	@Column(nullable= false)
	private String temp;
	@Column(nullable= false)
	private String sky;
	@Column(nullable= false)
	private String pty;
	@Column(nullable= false)
	private String rain;
	
	
	@OneToMany(mappedBy = "notice",fetch = FetchType.LAZY,cascade=CascadeType.ALL)
	@Builder.Default
	private List<detachfile> files=new ArrayList<detachfile>();
	
	@CreatedDate
	@Column(updatable = false,name = "red")//스프링부트말고 자바컬럼 업데이트시점에서 업데이트막음 
	private String red;
	//데이터포맷
	 @PrePersist
	  public void onpersist() {
	   this.red=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd/HH:mm:s"));
	 }
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="Member_id")
	@JsonIgnore
	private MemberEntity member;
	
	@OneToMany(mappedBy = "notice",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
	private List<CommentEntity> comments;
	
	
	//=================w좋아요 게시글에선삭제하면 좋아요없어지는게맞는듯===============================
	@OneToMany(mappedBy="notice",fetch = FetchType.LAZY,cascade =CascadeType.ALL)
	private List<FavoriteEntity> likeuser;
	
	
	
	public void addcomments(CommentEntity comment) {
		comments.add(comment);
	}
	public void addfiles(detachfile file) {
		
		files.add(file);
	}
	public void removefiles(Long id) {
		
		files.remove(id);
		
	}
	

	public NoticeDto toDto(Long num,
			String username,String nickname
			,String title, String text,String red,
			List<CommentEntity> comments,
			List<detachfile> detachfiles
			,int likes,String temp,String sky,String pty,String rain) {
		return NoticeDto.builder()
				.num(num)
				.username(username)
				.nickname(nickname)
				.title(title)
				.text(text)
				.red(red)
				.comments(comments)
				.detachfiles(detachfiles)
				.likes(likes)
				.temp(temp)
				.sky(sky)
				.pty(pty)
				.rain(rain)
				.build();
	}
	
	public NoticeDto toDto(Long num,String username,String nickname
			,String title, String text,String red
			,int likes,String temp,String sky,String pty,String rain) {
		return NoticeDto.builder()
				.num(num)
				.username(username)
				.nickname(nickname)
				.title(title)
				.text(text)
				.red(red)
				.likes(likes)
				.temp(temp)
				.sky(sky)
				.pty(pty)
				.rain(rain)
				.build();
	}
}
