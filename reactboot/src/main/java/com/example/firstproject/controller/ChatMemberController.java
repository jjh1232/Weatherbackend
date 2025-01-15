package com.example.firstproject.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.firstproject.Dto.ChatDto.ChatResponseDto;
import com.example.firstproject.Dto.ChatDto.ChatRoomDto;
import com.example.firstproject.Dto.ChatDto.Roomuseradd;
import com.example.firstproject.Dto.ChatDto.roomlistresponseDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Entity.StompRoom.Room;
import com.example.firstproject.Entity.StompRoom.chatmessage;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Service.Memberservice.MemberService;
import com.example.firstproject.Service.chatService.ChatService;
import com.example.firstproject.aop.Logoutano;
import com.example.firstproject.configure.PrincipalDetails;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatMemberController {

	private final MemberRepository memberrepository;
	
	private final ChatService chatservice;
	//채팅방 만들기 
	@PostMapping("/createchatroom")
	public Long createchatroom(Authentication authentication,@RequestBody ChatRoomDto dto) {//참여자리스트랑 채팅방이름 없으면 기본으로 유저아이디
		//리스트로 참여목록을 만든다 
		//리턴값은구지필요한지?
		log.info("채팅방생성");
		PrincipalDetails requestuser=(PrincipalDetails) authentication.getPrincipal();
		log.info("체크1");
		String roomname=requestuser.getMember().getNickname();
		log.info("체크2");
		String requestmembername=requestuser.getUsername();
		log.info("요청한 유저닉네임"+roomname);
		//깃체크용
		for(String nickname:dto.getUsernickname()) {
			log.info("채팅방이름닉네임:"+nickname);
			roomname +=","+nickname;
		}
		
		
		dto.getMemberlist().add(requestmembername);
		
		log.info("콘트롤러룸네임:"+roomname);
		
		Long roomid=chatservice.createChatroom(roomname,dto.getMemberlist());
		log.info("완료후룸아이디:"+roomid);
		return roomid;
		
	}
	//유저의채팅방목록검색 
	@GetMapping("/findchatroomlist")
	@Logoutano 
	public List<roomlistresponseDto> findchatroom(Authentication userdata) {
		log.info("유저데이터:"+userdata.getName());
		log.info("유저디테일"+userdata.getPrincipal());
		PrincipalDetails member=(PrincipalDetails) userdata.getPrincipal();
		Long memberid=member.getMember().getId();
		
		MemberEntity members=memberrepository.findById(memberid).orElseThrow();
		
		List<roomlistresponseDto> userroomlist=new ArrayList<>(); //리턴할 데이터형식
		
		for(MemberRoom list:members.getChatrooms()) {
			Room roomdata=list.getRoom();
			log.info("룸데이터:"+roomdata);
			log.info("룸데이터접속리스트:"+roomdata.getUserlist());
			List<chatmessage> chatdata=roomdata.getChatdata();
	
			chatmessage lately=chatdata.get(chatdata.size()-1);
			
			roomlistresponseDto dto = roomlistresponseDto.builder()
					.roomid(roomdata.getId())
					.roomname(roomdata.getRoomname())
					.namelist(roomdata.getUserlist())
					.time(roomdata.getUpdatedDate())
					.latelychat(lately.getMessage())
					.build();
			
			userroomlist.add(dto);
			
			
		}
		//람다함수로 
		//Collections.sort(userroomlist,(a,b)->b.getTime()-a.getTime());
			//내림차순 정렬!
		userroomlist.sort(Comparator.comparing(roomlistresponseDto::getTime).reversed());
		
		//스트림으로
		// List<roomlistresponseDto> sortlist=userroomlist.stream().sorted((a,b)->b.getTime()-a.getTime())
		//		.collect(Collectors.toList());
		
		return userroomlist;
		}
	
	//채팅방디테일
	@GetMapping("/chatroomdataget")
	public ResponseEntity chatroomdata(@RequestParam Long roomid){
		log.info("방데이터가죠오기");
		Room room=chatservice.findbychatroom(roomid);
		roomlistresponseDto roomdata=chatservice.roomdataget(room);
		//이것도 룸에서 바로가져와도될듯 
		List<ChatResponseDto> beforechat=chatservice.getbeforechat(room);
		Map<String,Object> data=new HashMap<>();
		data.put("roomdata", roomdata);
		data.put("beforechat",beforechat);
		return ResponseEntity.ok().body(data);
	}
	
	//채팅방나가기 
	@PostMapping("/chatroomexit")
	public ResponseEntity chatroomdelete(Authentication authentication,@RequestBody Map<String,String> data) {
		log.info("룸아이디:"+data.get("roomid"));
		String roomidstring=data.get("roomid");
		Long roomid=Long.parseLong(roomidstring);
		
		Room room=chatservice.findbychatroom(roomid);
		PrincipalDetails prin=(PrincipalDetails) authentication.getPrincipal();
		Long memberid=prin.getMember().getId();
		MemberEntity member=memberrepository.findById(memberid).orElseThrow();
		
		chatservice.roomuserexit(room, member);
		return ResponseEntity.ok("성공적");
	}
	
	//채티방 멤버추가하기
	@PostMapping("/chatroominvite")
	public ResponseEntity chatmemberadd(@RequestBody Roomuseradd adddto) throws Exception {
		
		log.info("유저초대!");
		
		Room roomdata=chatservice.Roomadduser(adddto.getUserlist(),adddto.getRoomid());
		
		roomlistresponseDto dto = roomlistresponseDto.builder()
				.roomid(roomdata.getId())
				.roomname(roomdata.getRoomname())
				.namelist(roomdata.getUserlist())
				.build();
		
		
		return ResponseEntity.ok().body(dto);
	}
	
}
