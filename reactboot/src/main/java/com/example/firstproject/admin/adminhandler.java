package com.example.firstproject.admin;

import java.util.Optional;

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
	//=================================멤버핸들러관리========================================
	public Page<MemberEntity> memberlistget(Pageable page){
		System.out.println("핸들러시작");
		Page<MemberEntity> memberlist=memberrepo.findAll(page);		
		System.out.println("리포종료");
		return memberlist;
		
	}
	
	public Page<MemberEntity> allusernamesearch(Pageable page,String keyword){
		Page<MemberEntity> memberlist=memberrepo.findByUsernameContaining(page,keyword);
		return memberlist;
		
	}
	public Page<MemberEntity> allnicknamesearch(Pageable page,String keyword){
		Page<MemberEntity> memberlist=memberrepo.findByNicknameContaining(page,keyword);
		return memberlist;
		
	}
	
	//멤버가입
	public MemberEntity membercreate(MemberEntity member){
		MemberEntity entity=memberrepo.save(member);
		return entity;
	}
	//=================================게시판페이지관리========================================
	public Page<NoticeEntity> noticeallget(Pageable page){
		
		Page<NoticeEntity> noticelist=noticerepo.findAll(page);
		
		return noticelist;
	}
	
	//==============================검색핸들러=====================================
	
	public Page<NoticeEntity> searchtitle(String text,Pageable pageable) {
		// TODO Auto-generated method stub
		
		Page<NoticeEntity> result=noticerepo.searchtitle(text,pageable);
		
		
		return result;
	}



	
	public Page<NoticeEntity> searchtitletext(String text,Pageable pageable) {
		
		Page<NoticeEntity> result=noticerepo.searchtitletext(text,pageable);
		return result;
	}



	
	public Page<NoticeEntity> searchtext(String text,Pageable pageable) {
			Page<NoticeEntity> result=noticerepo.searchtext(text,pageable);
			
		
		return result;
	}




	public Page<NoticeEntity> searchname(String text,Pageable pageable) {
			Page<NoticeEntity> result=noticerepo.searchname(text,pageable);
		
		
		return result;
	}
	//==============================검색핸들러=====================================
	
	public Optional<NoticeEntity> findbynotice(Long noticeid){
		Optional<NoticeEntity> entity=noticerepo.findById(noticeid);
		return entity;
	}
	
	public void deletenotice(NoticeEntity entity) {
		noticerepo.delete(entity);
	}
	//=================================게시판코멘트관리========================================
	public Page<CommentEntity> commentget(Pageable page){
		
		Page<CommentEntity> noticelist=commentrepo.findAll(page);
		
		return noticelist;
	}
	//=================================채팅방페이지관리========================================
	public Page<Room> chatroomallget(Pageable page){
		
		Page<Room> noticelist=roomrepo.findAll(page);
		
		return noticelist;
	}
//=========================================멤버삭제와 멤버엔티티찾기=========================
	public Optional<MemberEntity> findmember(Long id){
		Optional<MemberEntity> member=memberrepo.findById(id);
		return member;
	}
	
	public void memberdelete(MemberEntity member) {
		memberrepo.delete(member);
		
		
		
		
	}
}
