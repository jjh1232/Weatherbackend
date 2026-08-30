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
import com.example.firstproject.Entity.StompRoom.BaseTime;
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
public class CommentEntity extends BaseTime{
	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
	
	@Column(nullable = false)
	private int depth;
	
	
	
	@Column
	private Long cnum;
	
	@Column(nullable = false)
	private String username;
	
	@Column(nullable = false)
	private String nickname;
	
	@Column(nullable = false)
	private String text;
	
	//@CreatedDate
	//@Column(nullable = false)
	//private LocalDateTime redtime;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="notice_id" )
	@JsonIgnore
	private NoticeEntity notice;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="member_id")
	@JsonIgnore
	private MemberEntity member;
	
	//두가지다 false로 설정
	@Column(nullable=false,columnDefinition = "boolean default false")
	private boolean isdelete=false;

	/* 운영자 차단.
	   isdelete 를 재활용하면 안 된다. 그러면 운영자가 가린 댓글이
	   "삭제된 댓글입니다" 로 보여서 작성자가 스스로 지운 것처럼 읽힌다.
	   삭제와 달리 되돌릴 수 있어야 하므로(오차단) 원문은 그대로 두고
	   보여줄 때만 안내 문구로 바꿔치운다. */
	@Column(nullable=false,columnDefinition = "boolean default false")
	private boolean isblocked=false;
	
	
	
	public CommentDto toDto(Long id,int depth,Long cnum,String username,String nickname,
			String text,String redtime,String userprofile) {
		return CommentDto.builder()
				.id(id)
			
				.depth(depth)
				.cnum(cnum)
				.username(username)
				.nickname(nickname)
				.text(text)
				.redtime(redtime)
				.userprofile(userprofile)
				//관리자 화면은 원문을 그대로 보되(그래야 판단이 된다)
				//차단/삭제 상태는 알아야 버튼을 맞게 그린다
				.isdelete(this.isdelete)
				.isblocked(this.isblocked)
				.build();
	}

	
	
	
	

}
