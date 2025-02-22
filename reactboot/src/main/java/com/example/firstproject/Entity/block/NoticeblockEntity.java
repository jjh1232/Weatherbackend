package com.example.firstproject.Entity.block;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.StompRoom.BaseTime;
import com.example.firstproject.Entity.block.BlockEnum.NoticeblockEnum;
import com.example.firstproject.Service.Blockservice.converter.BlocknoticeConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.qos.logback.core.subst.Token.Type;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class NoticeblockEntity extends BaseTime{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="Member_id")
	@JsonIgnore
	private MemberEntity member;
	


	private Long noticeid;
	
	//@Enumerated(EnumType.STRING)//이넘셋쓸떈 당연히필요없음
	@Builder.Default
	@Convert(converter=BlocknoticeConverter.class) //콘버터설정
	//@ElementCollection//값타임컬렉션 db가 컬렉션을 인지못해서 이거넣는다고함 근데테이블이생성되서 안쓸듯
	private EnumSet<NoticeblockEnum> reason;
	
	public void addreason(NoticeblockEnum data) {
		//this.reason.add(data);
	}
	
}
