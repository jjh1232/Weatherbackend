package com.example.firstproject.admin;

import java.util.Optional;

import javax.xml.stream.events.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.detachfile;
import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Entity.StompRoom.Room;
import com.example.firstproject.Entity.StompRoom.chatmessage;
import com.example.firstproject.Repository.CommentRepository;
import com.example.firstproject.Repository.DetachfileRepository;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Repository.NoticeRepository;
import com.example.firstproject.Repository.roomrepo.ChatMessageRepository;
import com.example.firstproject.Repository.roomrepo.ChatRoomRepository;
import com.example.firstproject.Repository.roomrepo.MemberRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class adminhandler {

	private final MemberRepository memberrepo;
	
	private final NoticeRepository noticerepo;
	
	private final CommentRepository commentrepo;
	
	private final ChatRoomRepository roomrepo;
	
	private final MemberRoomRepository memberroomrepo;
	
	private final ChatMessageRepository chatrepo;
	
	private final DetachfileRepository detachrepo;
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
	//유저아이디로멤버검색
	public Optional<MemberEntity> usernamefind(String username){
		Optional<MemberEntity> member=memberrepo.findByUsername(username);
		return member;
	}
	//=================================게시판페이지관리========================================
	public Page<NoticeEntity> noticeallget(Pageable page){
		
		Page<NoticeEntity> noticelist=noticerepo.findAll(page);
		
		return noticelist;
	}
	public NoticeEntity noticedetail(Long noticeid) throws IllegalAccessException {
		NoticeEntity entity=noticerepo.findById(noticeid).orElseThrow(()->new IllegalAccessException("게시글업승"));
		return entity;
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


	public Page<NoticeEntity> searchusername(MemberEntity member,Pageable page){
		Page<NoticeEntity> result =noticerepo.findByMember(member, page);
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
	//======================================================================================
		public Page<CommentEntity> commentget(Pageable page){
		
		Page<CommentEntity> noticelist=commentrepo.findAll(page);
		
		return noticelist;
	}
		//이메일검색
		public Page<CommentEntity> emailcomment(Pageable page,String keyword){
			
			Page<CommentEntity> entity=commentrepo.findByUsernameContaining(page, keyword);
			
			return entity;
		}
		//텍스트검색
		public Page<CommentEntity> nicknamecomment(Pageable page,String keyword){

			Page<CommentEntity> entity=commentrepo.findByNicknameContaining(page, keyword);
			return entity;
		}
		
		//게시글번호검색
		public Page<CommentEntity> noticenumcomment(Pageable page,Long keyword){

			Page<CommentEntity> entity=commentrepo.findByNoticeIdContaining(page, keyword);
			return entity;
		}
		
		//코멘트찾기 
		public Optional<CommentEntity> commentfind(Long commentid){
			Optional<CommentEntity> entity=commentrepo.findById(commentid);
			return entity;
		}
		
		//댓글삭제핸들러
		public void deletecomment(CommentEntity comment) {
			commentrepo.delete(comment);
			
		}
		
		//부적절한 이미지 변경
		public Optional<detachfile> detachget(Long detachid){
			Optional<detachfile> deta=detachrepo.findById(detachid);
			return deta;
		}
		
	//=================================채팅방페이지관리========================================
	public Page<Room> chatroomallget(Pageable page){
		
		Page<Room> noticelist=roomrepo.findAll(page);
		
		return noticelist;
	}
	//채팅방이름검색
	public Page<Room> roomnamefind(Pageable page,String keyword){
		Page<Room> room=roomrepo.findByRoomnameContaining(page, keyword);
		
		return room;
	}//참가리스트검색
	public Page<MemberRoom> roomnamelistfind(Pageable page,String nickname ){
		//멤버룸에서가져와야함
		Page<MemberRoom> namelist=memberroomrepo.findByMembernickname(page,nickname );
		return namelist;
		}
	
	public Page<MemberRoom> roomusernametfind(Pageable page,MemberEntity member ){
		//멤버룸에서가져와야함
		Page<MemberRoom> namelist=memberroomrepo.findByMember(page,member );
		return namelist;
		}
	//채팅텍스트검색================================================================
	public Page<chatmessage> roomchatfind(Pageable page,String keyword){
			//챗메세지타입에서가져오기
		Page<chatmessage> chat=chatrepo.findByMessageContaining(page, keyword);
		return chat;
	}
	//룸찾기
	public Optional<Room> roomget(Long loomid) {
		Optional<Room> entity=roomrepo.findById(loomid);
		return entity;
	}
	
	//룸삭제
	public void deleteroom(Room roomentity) {
		roomrepo.delete(roomentity);
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
