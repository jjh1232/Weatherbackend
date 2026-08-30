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
import com.example.firstproject.Entity.block.NoticedecleEntity;
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
	
	@Column(nullable= false)
	private String reh;
	@Column(nullable= false)
	private String wsd;
	
	@OneToMany(mappedBy = "notice",fetch = FetchType.LAZY,cascade=CascadeType.ALL)
	@Builder.Default
	private List<detachfile> files=new ArrayList<detachfile>();
	
	@CreatedDate
	/* 작성일시를 문자열로 담는다. 포맷의 초가 "s"(한 자리)라
	   같은 분 안에서는 문자열 정렬이 뒤집힌다("19:05:9" > "19:05:10").
	   그래서 목록 정렬은 red 가 아니라 식별자 DESC 를 쓴다.
	   정렬 프로퍼티는 "noticeid"(필드명) 가 아니라 "id" 로 준다.
	   생성자 표현식(select new ...) 쿼리에서는 정렬 구문이 번역 없이 SQL 로 나가는데,
	   실제 컬럼명이 id 라서 "noticeid" 로 주면 Unknown column 오류가 난다. */
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
	
	//임시어거지용
	//신고당한정보
	@OneToMany(mappedBy="notice",fetch=FetchType.LAZY,cascade = CascadeType.ALL)
	private List<NoticedecleEntity> decles;
	
	//조회수
	@Column(nullable= false)
	private long views;
	
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
			,int likes,String temp,String sky,String pty,String rain,String reh,String wsd,long views) {
		return NoticeDto.builder()
				.num(num)
				.username(username)
				.nickname(nickname)
				.title(title)
				.text(text)
				.red(red)
				//.comments(comments)
				.detachfiles(detachfiles)
				.likes(likes)
				.temp(temp)
				.sky(sky)
				.pty(pty)
				.rain(rain)
				.reh(reh)
				.wsd(wsd)
				.views(views)
				.build();
	}
	
	public NoticeDto toDto(Long num,String username,String nickname
			,String title, String text,String red
			,int likes,String temp,String sky,String pty,String rain,long views) {
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
				.views(views)
				.build();
	}
}
