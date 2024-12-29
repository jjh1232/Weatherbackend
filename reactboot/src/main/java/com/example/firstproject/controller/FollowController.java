package com.example.firstproject.controller;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.follow.FollowDto;
import com.example.firstproject.Dto.follow.findDto;
import com.example.firstproject.Dto.follow.followlistDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.follow.FollowEntity;
import com.example.firstproject.Service.Followservice.FollowService;
import com.example.firstproject.Service.Memberservice.MemberService;
import com.example.firstproject.aop.Logoutano;
import com.example.firstproject.configure.PrincipalDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FollowController {

	private final MemberService memberservice;
	
	private final FollowService followservice;
	//친구맺기!
	@GetMapping("/follow")
	public ResponseEntity follow(Authentication authentication,@RequestParam String friendname) throws Exception {
		log.info("인증유저겟네임:"+authentication.getName());
		
		MemberEntity frommember=memberservice.findemail(authentication.getName()).orElseThrow();		
		
		MemberEntity tomember=memberservice.findemail(friendname).orElseThrow();
		
		followservice.follow(frommember,tomember);
		
		return ResponseEntity.ok().build();
	}
	
	//팔로잉조회
	@GetMapping("/followlist")
	@Logoutano 
	public ResponseEntity followlist(Authentication authentication) {
		log.info("팔로우리스트");
		MemberEntity member=memberservice.findemail(authentication.getName()).orElseThrow();
		List<followlistDto> listname=new ArrayList<>();
		for (FollowEntity entity:member.getFollowings()){
			System.out.println(entity.getTomember().getUsername());
			followlistDto listdto=followlistDto.builder()
					.username(entity.getTomember().getUsername())
					.nickname(entity.getTomember().getNickname())
					.favorite(entity.isFavorite())
					.build();
			listname.add(listdto);
			log.info("즐겨찾기여부:"+listdto.isFavorite());
		}
		return ResponseEntity.ok(listname);
	}
	//팔로워조회 내가당한거팔로우 목록도 필요함
	@GetMapping("/followerlist")
	@Logoutano 
	public ResponseEntity followerlist(Authentication authentication) {
		log.info("팔로워리스트");
		PrincipalDetails member=(PrincipalDetails) authentication.getPrincipal();
		
		List<FollowDto> nicklist=followservice.followerfind(member.getMember().getId());
				return ResponseEntity.ok(nicklist);
	}
	//팔로우취소
	
	@DeleteMapping("/followdelete/{friendname}")
	public ResponseEntity deletefollow(Authentication authentication,@PathVariable String friendname) {
		log.info("팔로우삭제");
		MemberEntity frommember=memberservice.findemail(authentication.getName()).orElseThrow();		
		
		MemberEntity tomember=memberservice.findemail(friendname).orElseThrow();
		
		followservice.followdelete(frommember, tomember);
		
		return null;
		
	}
	
	@GetMapping("/open/usersearch")
	@Logoutano 
	public ResponseEntity usersearch(@RequestParam String nickname) {
	
		if(nickname=="") {
			log.info("입력값업슴!");
			return null;
		}
		else {
			log.info(nickname);
		List<findDto> list=memberservice.findbynickname(nickname);
		log.info("여기");
		return ResponseEntity.ok().body(list);
		}
	}
	//개인팔로우여부 체크
	@GetMapping("/followcheck")
	@Logoutano 
	public ResponseEntity followcheck(Authentication authentication,@RequestParam String friendname) {
		MemberEntity frommember=memberservice.findemail(authentication.getName()).orElseThrow();		
		
		MemberEntity tomember=memberservice.findemail(friendname).orElseThrow();
		
		boolean followcheck=followservice.flowcheck(frommember, tomember);
		
		return ResponseEntity.ok().body(followcheck);
	}
	
		//팔로우 즐겨찾기 기능!
	@GetMapping("/favoritefollow/{friendname}")
	@Logoutano 
	public ResponseEntity followfavorite(Authentication authentication,@PathVariable String friendname) {
		PrincipalDetails fromuser=(PrincipalDetails) authentication.getPrincipal();
		MemberEntity loginuser=fromuser.getMember();
		MemberEntity tomember=memberservice.findemail(friendname).orElseThrow();
		
		FollowEntity entity=followservice.followfind(loginuser,tomember);
		
		
		return ResponseEntity.ok("성공");
	}
	@GetMapping("/favoriteunfollow/{friendname}")
	public ResponseEntity followunfavorite(Authentication authentication,@PathVariable String friendname) {
		PrincipalDetails fromuser=(PrincipalDetails) authentication.getPrincipal();
		MemberEntity loginuser=fromuser.getMember();
		MemberEntity tomember=memberservice.findemail(friendname).orElseThrow();
		
		FollowEntity entity=followservice.unfollowfind(loginuser,tomember);
		
		
		return ResponseEntity.ok("성공");
	}
	
	@GetMapping("/favoritelist")
	@Logoutano 
	public ResponseEntity favoritelist(Authentication authentication) {
		MemberEntity member=memberservice.findemail(authentication.getName()).orElseThrow();
		//겟불러오는거보단 따로 쿼리만드는게 싸지않을까?
		List<followlistDto> list=followservice.favoritefollow(member);
		
		
		return ResponseEntity.ok().body(list);
	
	
	}
						
			
		
		
	
}
