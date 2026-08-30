package com.example.firstproject.Entity;

import java.time.LocalDateTime;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;

import com.example.firstproject.Entity.StompRoom.BaseTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="detachfiles")
public class detachfile extends BaseTime{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;
	
	@Column
	public Long idx;//글내부아이디
	@Column
	public Long rangeindex; //글내부 데이터 위치를위한인덱스 
	
	@Column(nullable = false)
	public String filename;
	
	@Column(nullable = false)
	public String path;

	/* 차단 전 원본 경로. 차단할 때만 채워지고 복구하면 다시 비운다.
	   이걸 안 남기면 path 와 글 본문이 둘 다 차단 이미지로 덮여서
	   원본이 어느 파일이었는지 알 방법이 사라진다(오차단을 못 되돌린다).
	   기존 행은 null 이므로 nullable 이어야 한다. */
	@Column(nullable = true)
	public String originalpath;
	
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="notice_id" )
	@JsonIgnore
	private NoticeEntity notice;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="member_id")
	@JsonIgnore
	private MemberEntity member;
	
	
}
