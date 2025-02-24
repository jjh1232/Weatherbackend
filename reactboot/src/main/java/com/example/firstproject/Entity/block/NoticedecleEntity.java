package com.example.firstproject.Entity.block;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.StompRoom.BaseTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/*
@Table(
        name = "tablename",
        uniqueConstraints = {
                @UniqueConstraint(name = "multiUniqueConstraint", columnNames = {
                        "NFT_NAME", "NFT_SUB_NAME"
                })
        },
       
)
*/ //다중유니크컬럼하는법 근데난테이블에서만들어서 ㅇㅇ;
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class NoticedecleEntity extends BaseTime{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	//차단한유저
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="Member_id")
	@JsonIgnore
	private MemberEntity member;
	
	//차단한 게시글
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="Notice_id")
	@JsonIgnore
	private NoticeEntity notice;
	
	//이유
	private String reason;
}
