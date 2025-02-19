package com.example.firstproject.Entity.block;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.example.firstproject.Entity.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.qos.logback.core.subst.Token.Type;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
public class NoticeblockEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="Member_id")
	@JsonIgnore
	private MemberEntity member;
	


	private Long noticeid;
}
