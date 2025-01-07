package com.example.firstproject.admin;

import javax.xml.stream.events.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.StompRoom.Room;
import com.example.firstproject.Repository.CommentRepository;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Repository.NoticeRepository;
import com.example.firstproject.Repository.roomrepo.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class adminhandler {

	private final MemberRepository memberrepo;
	
	private final NoticeRepository noticerepo;
	
	private final CommentRepository commentrepo;
	
	private final ChatRoomRepository roomrepo;
	
	public Page<MemberEntity> memberlistget(Pageable page){
		System.out.println("핸들러시작");
		Page<MemberEntity> memberlist=memberrepo.findAll(page);		
		System.out.println("리포종료");
		return memberlist;
		
	}
	
	public Page<NoticeEntity> noticeallget(Pageable page){
		
		Page<NoticeEntity> noticelist=noticerepo.findAll(page);
		
		return noticelist;
	}
	public Page<CommentEntity> commentget(Pageable page){
		
		Page<CommentEntity> noticelist=commentrepo.findAll(page);
		
		return noticelist;
	}
	public Page<Room> chatroomallget(Pageable page){
		
		Page<Room> noticelist=roomrepo.findAll(page);
		
		return noticelist;
	}
}
