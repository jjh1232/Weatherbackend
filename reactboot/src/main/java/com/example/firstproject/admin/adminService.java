package com.example.firstproject.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.ChatDto.ChatRoomDto;
import com.example.firstproject.Dto.ChatDto.roomlistresponseDto;
import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.StompRoom.Room;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class adminService {
	
	
	private final adminhandler adminhandler;
	
	
	public Page<MemberDto> allmemberget(int page) {
	
		System.out.println("핸들러시작");
		Pageable pageable=PageRequest.of(page-1, 20,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"regdate" ));
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
	public Page<NoticeDto> allnoticeget(int page) {
		
		System.out.println("핸들러시작");
		Pageable pageable=PageRequest.of(page-1, 20,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"red" ));
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
	

}
