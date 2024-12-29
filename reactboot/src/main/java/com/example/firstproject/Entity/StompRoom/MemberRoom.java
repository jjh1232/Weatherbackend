package com.example.firstproject.Entity.StompRoom;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.example.firstproject.Entity.Address;
import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.Notification;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRoom extends BaseTime{

	//중간테이블 manytomnay가 자동으로 만들어주긴하지만 실제론 여기서 필요한 메소드가 있을수도있기때문에 직접만들어야함 ex)업데이트시간같은ㅌ거
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String membernickname;
	
	private String roomname;
	
	@ManyToOne
	//@Cascade(CascadeType.PERSIST)
	@JoinColumn(name="member_id")//이거자기컬럼이름이고 주인외래키지정은 referencedColumnName의default값임
								//그냥실행되는이유는 선언한값이 엔티티에있으면알아서하기때문
	@JsonIgnore//멤버에서멤버룸을ㅊ자고또여기서멤버를찾는 무한참조가일어남 때문에 여기서 null을주는 이거써야함				
	private MemberEntity member;
	
	@ManyToOne
	@JoinColumn(name="room_id")
	//@Cascade(CascadeType.PERSIST)
	@JsonIgnore
	private Room room;

	
}
