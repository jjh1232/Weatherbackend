package com.example.firstproject.Entity.StompRoom;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

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
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room extends BaseTime{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="room_id")
	private Long id;
	
	private String roomname;
	
	
	
	@Builder.Default //초기화값을 빌더는제공하지않아서 이거추가해야함
	@OneToMany(mappedBy="room", fetch=FetchType.LAZY,cascade = CascadeType.ALL)
	private Set<MemberRoom> userlist=new HashSet<>(); //유저리스트
	
	//룸삭제시채팅도삭제해야
	@OneToMany(mappedBy="room",fetch=FetchType.LAZY,cascade = CascadeType.ALL)
	private List<chatmessage> chatdata=new ArrayList<>();//채팅불러오기
	
	public void adduserlist(MemberRoom entity) {
		this.userlist.add(entity);
	}
	
	
	

	
	
}
