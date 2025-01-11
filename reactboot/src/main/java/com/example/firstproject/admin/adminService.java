package com.example.firstproject.admin;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.ChatDto.ChatRoomDto;
import com.example.firstproject.Dto.ChatDto.roomlistresponseDto;
import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Entity.Address;
import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Entity.StompRoom.Room;
import com.example.firstproject.Handler.MemberHandler;
import com.example.firstproject.admin.form.Admemberupdateform;
import com.example.firstproject.admin.form.Adminmembercreateform;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class adminService {
	
	
	private final adminhandler adminhandler;
	
	
	private final BCryptPasswordEncoder passen;
	//==================================//멤버=========================================
	
	public Page<MemberDto> allmemberget(int page) {
	
		System.out.println("핸들러시작");
		Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"regdate" ));
		Page<MemberEntity> memberentity=adminhandler.memberlistget(pageable);
	
		Page<MemberDto> memberlist=memberentity.map((m)->
			MemberDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.role(m.getRole())
			.provider(m.getProvider())
			.red(m.getRegdate())
			.homeaddress(m.getHomeaddress())
			.usernotice(m.getNotices().size())
			.usercomments(m.getComments().size())
			.userchatroom(m.getChatrooms().size())
			.build());	
				
			
		

		
		return memberlist;
		
	}
	
	//==============멤버 검색============================
	public Page<MemberDto> searchmembers(String option,String keyword,int page){
		Pageable pageable=PageRequest.of(page-1, 20,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"regdate" ));
		
		if(option.equals("email")){
			
			Page<MemberEntity> memberentity=adminhandler.allusernamesearch(pageable,keyword);
			Page<MemberDto> memberlist=memberentity.map((m)->
			MemberDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.role(m.getRole())
			.provider(m.getProvider())
			.red(m.getRegdate())
			.homeaddress(m.getHomeaddress())
			.usernotice(m.getNotices().size())
			.usercomments(m.getComments().size())
			.userchatroom(m.getChatrooms().size())
			.build());	

			return memberlist;
		}
		else if(option.equals("nickname")) {
			Page<MemberEntity> memberentity=adminhandler.allnicknamesearch(pageable,keyword);
		
			Page<MemberDto> memberlist=memberentity.map((m)->
			MemberDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.role(m.getRole())
			.provider(m.getProvider())
			.red(m.getRegdate())
			.homeaddress(m.getHomeaddress())
			.usernotice(m.getNotices().size())
			.usercomments(m.getComments().size())
			.userchatroom(m.getChatrooms().size())
			.build());	

			return memberlist;
		}
		
		return null;
	}
	
	//멤버생성=======================================================
	public String membercreate(Adminmembercreateform form) {
		
		String newpass=passen.encode(form.getPassword());//시큐리티로그인도 인코딩해줌
		Address regions=new Address();
	
		if(form.getRegion().equals("")) {
			System.out.println("레기온빈값");
			regions=Address.builder().juso("서울특별시  종로구  청운효자동").gridx("60").gridy("127").build();
		}
		else {
			System.out.println("레기온있음");
		 regions=Address.builder().juso(form.getRegion()).gridx(form.getGridx()).gridy(form.getGridy())
		.build();
		}
		System.out.println("크레잇서비스주소확인:"+form.getRegion());
		MemberEntity entity=MemberEntity.builder()
				
				.username(form.getUsername())
				
				.nickname(form.getNickname())
				.password(newpass)
				
				.provider(form.getProvider())
				.providerid(null)
				.homeaddress(regions)
				.role(form.getRole())
				
				.build();
		
		MemberEntity okentity=adminhandler.membercreate(entity);
		
		return okentity.getNickname();
	}
	
	//멤버정보 업데이트 
	@Transactional
	public String memberupdate(Long userid,Admemberupdateform form) throws IllegalAccessException {
	
		Address regions=Address.builder().juso(form.getRegion()).gridx(form.getGridx()).gridy(form.getGridy())
				.build();
	
		MemberEntity okentity=adminhandler.findmember(userid).orElseThrow(()->new IllegalAccessException("해당하는유저없음"));
	
		okentity.setUsername(form.getUsername());
		
		okentity.setNickname(form.getNickname());
		okentity.setProvider(form.getProvider());
		okentity.setHomeaddress(regions);
		okentity.setRole(form.getRole());
		
		return "성공적으로변경";
	}
	
	
	//=================================게시판페이지관리========================================
	public Page<NoticeDto> allnoticeget(int page) {
		
		System.out.println("핸들러시작");
		Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"red" ));
		Page<NoticeEntity> entity=adminhandler.noticeallget(pageable);
	
		Page<NoticeDto> list=entity.map((m)->
				NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
				.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
				.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
				.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(m.getFiles())
				.comments(m.getComments()).userprofile(m.getMember().getProfileimg()).build()
				);
			
		

		
		return list;
		
	}
	
	//===============================================게시글검색==================================
	public Page<NoticeDto> searchnotice(int page,String option,String keyword){
		Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"red"));
		//케이스문변수중복이안됨;;
		if (option.equals("titletext")) {
			
			String option1="title";
			String option2="text";
			Page<NoticeEntity> entity=adminhandler.searchtitletext(keyword,pageable);
			Page<NoticeDto> list=entity.map((m)->
			NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
			.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
			.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
			.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(m.getFiles())
			.comments(m.getComments())
			.userprofile(m.getMember().getProfileimg())
			
			.build()
			);
		
			return list;
			
		}
		else if(option.equals("title")){
			
					
			Page<NoticeEntity> entity=adminhandler.searchtitle(keyword,pageable);
			Page<NoticeDto> list=entity.map((m)->
			NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
			.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
			.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
			.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(m.getFiles())
			.comments(m.getComments()).userprofile(m.getMember().getProfileimg()).build()
			);
			return list;
		}
		else if(option.equals("text")) {
			
			Page<NoticeEntity> entity=adminhandler.searchtext(keyword,pageable);
			
			Page<NoticeDto> dtlist=entity.map(m-> new NoticeDto(m));
			Page<NoticeDto> list=entity.map((m)->
			NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
			.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
			.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
			.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(m.getFiles())
			.comments(m.getComments()).userprofile(m.getMember().getProfileimg()).build()
			);
			return list;
		
		}
		else {
			
			Page<NoticeEntity> entity=adminhandler.searchname(keyword,pageable);
			Page<NoticeDto> list=entity.map((m)->
			NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
			.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
			.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
			.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(m.getFiles())
			.comments(m.getComments()).userprofile(m.getMember().getProfileimg()).build()
			);
			
			return list;
			
		}

	
		
	}
	//==============게시글삭제
	public void deletenotice(Long noticeid) throws IllegalAccessException {
		NoticeEntity entity= adminhandler.findbynotice(noticeid).orElseThrow(()->new IllegalAccessException("게시글이없습니다"));
		
		adminhandler.deletenotice(entity);
		
	}
	
	
	//=================================게시판댓글관리========================================
	//=======================================================================================
	public Page<CommentDto> allCommentrget(int page) {
		
		
		Pageable pageable=PageRequest.of(page-1, 20,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate" ));
		Page<CommentEntity> entity=adminhandler.commentget(pageable);
	
		Page<CommentDto> list=entity.map((m)->
			CommentDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.text(m.getText()).depth(m.getDepth()).cnum(m.getCnum())
			.redtime(m.getCreatedDate()).userprofile(m.getMember().getProfileimg())
			.noticenum(m.getNotice().getNoticeid())
					
			.build());	
				
			
		

		
		return list;
		
	}
	//게시글검색조건===========================================================================
	public Page<CommentDto> commentsearch(int page,String option,String keyword){
		
	
	
		if(option.equals("email")) {
			Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate" ));
			Page<CommentEntity> entity=adminhandler.emailcomment(pageable, keyword);
			Page<CommentDto> list=entity.map((m)->
			CommentDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.text(m.getText()).depth(m.getDepth()).cnum(m.getCnum())
			.redtime(m.getCreatedDate()).userprofile(m.getMember().getProfileimg())
			.noticenum(m.getNotice().getNoticeid())
					
			.build());	
				
					
			return list;
		}else if(option.equals("nickname")) {
			Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate" ));
			Page<CommentEntity> entity=adminhandler.nicknamecomment(pageable, keyword);
			Page<CommentDto> list=entity.map((m)->
			CommentDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.text(m.getText()).depth(m.getDepth()).cnum(m.getCnum())
			.redtime(m.getCreatedDate()).userprofile(m.getMember().getProfileimg())
			.noticenum(m.getNotice().getNoticeid())
					
			.build());	
				
			return list;
		}else if (option.equals("noticenum")) {
			//리포지토리에서 노티스객체를 못받아서 네이티브쿼리로처리했는데 그럴시 page객체에 sql문 원문그대로놓아줘야하는듯?
			//어쩔수없이 if문마다 다르게 pageable작성
			Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"created_date" ));
			
			System.out.println("게시글번호:"+keyword);
			Long noticeid=Long.parseLong(keyword);
			Page<CommentEntity> entity=adminhandler.noticenumcomment(pageable, noticeid);
			Page<CommentDto> list=entity.map((m)->
			CommentDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.text(m.getText()).depth(m.getDepth()).cnum(m.getCnum())
			.redtime(m.getCreatedDate()).userprofile(m.getMember().getProfileimg())
			.noticenum(m.getNotice().getNoticeid())
					
			.build());	
				
			return list;
		}
		else {
			System.out.println("잘못된검색옵션입니다");
			return null;
		}
			}
	
	//============================채티방관련 서비스================================================
	//기본채팅방
	public Page<roomlistresponseDto> allRoomget(int page) {
		
		System.out.println("핸들러시작");
		Pageable pageable=PageRequest.of(page-1, 20,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate" ));
		Page<Room> entity=adminhandler.chatroomallget(pageable);
	
		Page<roomlistresponseDto> list=entity.map((m)->
		roomlistresponseDto.builder().roomid(m.getId()).roomname(m.getRoomname())
		.namelist(m.getUserlist()).red(m.getCreatedDate())
		.chatnum(m.getChatdata().size())
		.latelychat(m.getChatdata().get(m.getChatdata().size()-1).getMessage())
		.lastchatred(m.getChatdata().get(m.getChatdata().size()-1).getCreatedDate())
		.build());
				
			
		

		
		return list;
		
	}
	//채팅검색 시
	public Page<roomlistresponseDto> searchrooms(int page,String option,String keyword) throws IllegalAccessException{
		Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate" ));
		if(option.equals("roomname")) {
			Page<Room> entity=adminhandler.roomnamefind(pageable, keyword);
			Page<roomlistresponseDto> list=entity.map((m)->
			roomlistresponseDto.builder().roomid(m.getId()).roomname(m.getRoomname())
			.namelist(m.getUserlist()).red(m.getCreatedDate())
			.chatnum(m.getChatdata().size())
			.latelychat(m.getChatdata().get(m.getChatdata().size()-1).getMessage())
			.lastchatred(m.getChatdata().get(m.getChatdata().size()-1).getCreatedDate())
			.build());
			
			return list;
			
		}
		else if(option.equals("partilist")) {
			MemberEntity member=adminhandler.usernamefind(keyword).orElseThrow(()->
				new IllegalAccessException("해당하는회원이없습니다")
			);
			
			Page<MemberRoom> entity=adminhandler.roomnamelistfind(pageable, member);
			Page<roomlistresponseDto> list=entity.map((m)->
			roomlistresponseDto.builder().roomid(m.getRoom().getId())
			.roomname(m.getRoom().getRoomname())
			.namelist(m.getRoom().getUserlist()).red(m.getRoom().getCreatedDate())
			.chatnum(m.getRoom().getChatdata().size())
			.latelychat(m.getRoom().getChatdata().get(m.getRoom().getChatdata().size()-1).getMessage())
			.lastchatred(m.getRoom().getChatdata().get(m.getRoom().getChatdata().size()-1).getCreatedDate())
			.build());
			return list;
		}
		/*
		else if(option.equals("chattext")) {
			adminhandler.roomchatfind(pageable, keyword);
		}
		*/
		else {
			System.out.println("올바르지않은검색입니다");
			return null;
		}
	}
	
	
	//==================================멤버삭제 서비스=================================
	
	
	
	public String memberdelete(Long userid) throws IllegalAccessException {
		MemberEntity member=adminhandler.findmember(userid).orElseThrow(()->
			 new IllegalAccessException("회원이없습니다")
		);
		
		adminhandler.memberdelete(member);
		
		return "멤버삭제성공";
	}

}
