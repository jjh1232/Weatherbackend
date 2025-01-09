package com.example.firstproject.admin;

import java.util.List;

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
import com.example.firstproject.Entity.StompRoom.Room;
import com.example.firstproject.Handler.MemberHandler;
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
			.build());	

			return memberlist;
		}
		
		return null;
	}
	
	//멤버생성=======================================================
	public String membercreate(Adminmembercreateform form) {
		
		String newpass=passen.encode(form.getPassword());//시큐리티로그인도 인코딩해줌
		Address regions=Address.builder().juso(form.getRegion()).gridx(form.getGridx()).gridy(form.getGridy())
		.build();
		
		
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
			.comments(m.getComments()).userprofile(m.getMember().getProfileimg()).build()
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
	public Page<CommentDto> allCommentrget(int page) {
		
		
		Pageable pageable=PageRequest.of(page-1, 20,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"regdate" ));
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
	
	public Page<roomlistresponseDto> allRoomget(int page) {
		
		System.out.println("핸들러시작");
		Pageable pageable=PageRequest.of(page-1, 20,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate" ));
		Page<Room> entity=adminhandler.chatroomallget(pageable);
	
		Page<roomlistresponseDto> list=entity.map((m)->
		roomlistresponseDto.builder().roomid(m.getId()).roomname(m.getRoomname())
		.namelist(m.getUserlist()).red(m.getCreatedDate())
		.build());
				
			
		

		
		return list;
		
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
