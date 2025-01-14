package com.example.firstproject.admin;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.ChatDto.roomlistresponseDto;
import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.admin.form.Admemberupdateform;
import com.example.firstproject.admin.form.Adminmembercreateform;
import com.example.firstproject.admin.form.AdminnoticeUpdateform;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class admincontroller {

	private final adminService adminservice;
	
	@GetMapping("/main")
	public ResponseEntity getmain() {
		
		
		return null;
	}
	//=================================멤버페이지관리========================================
	@GetMapping("/membermanage")
	public ResponseEntity memberpage(@RequestParam(defaultValue="1") int page,
			@RequestParam(required = false) String option,
			@RequestParam(required =false) String searchtext) {
		
		System.out.println("어드민시작");
		
		
		if(searchtext==null) {
			System.out.println("검색어없음");
			Page<MemberDto> memberlist=adminservice.allmemberget(page);
			return ResponseEntity.ok(memberlist);
		}
		else {
			System.out.println("검색데이터");
			Page<MemberDto> memberlist=adminservice.searchmembers(option,searchtext,page);
			return ResponseEntity.ok(memberlist);
		}
		
		
	}
	
	//어드민권한으로 회원 만들기
	@PostMapping("/membercreate")
	public ResponseEntity membercreate(@Valid @RequestBody Adminmembercreateform form) {
		
		
		String nickname=adminservice.membercreate(form);
		
		return ResponseEntity.ok(nickname+"으로 회원가입되었습니다");
	}
	//어드민권한으로 회원정보수정하기
	@PutMapping("/memberupdate/{userid}")
	public ResponseEntity memberupdate(@PathVariable long userid,@RequestBody Admemberupdateform form) throws IllegalAccessException {
		
		adminservice.memberupdate(userid, form);
		
		return null;
	}
	//=================================게시판페이지관리========================================
	@GetMapping("/noticemanage")
	public ResponseEntity noticemanage(@RequestParam(defaultValue="1") int page,
			@RequestParam(required = false) String option,
			@RequestParam(required =false) String searchtext
			) throws IllegalAccessException {
		System.out.println("노티스컨트롤페이지:"+page);
		System.out.println("노티스컨트롤서치:"+searchtext);
		
		if(searchtext==null) {
			Page<NoticeDto> memberlist=adminservice.allnoticeget(page);
			return ResponseEntity.ok(memberlist);
		}else {
			Page<NoticeDto> memberlist=adminservice.searchnotice(page,option,searchtext);
			
			return ResponseEntity.ok(memberlist);
		}
		
		
		
		 
	}
	@GetMapping("/noticedetail/{noticeid}")
	public NoticeDto noticedetailget(@PathVariable Long noticeid) throws IllegalAccessException {
		NoticeDto dto=adminservice.getnoticedetail(noticeid);
		
		return dto;
	}
	//수정
	@PutMapping("/noticeupdate/{noticeid}")
	public ResponseEntity noticeupdate(@PathVariable Long noticeid,@RequestBody AdminnoticeUpdateform form) throws IllegalAccessException {
		System.out.println("어드민게시글업데이트");
		adminservice.noticeupdate(noticeid,form);
		
		return ResponseEntity.ok("수정완료");
	}
	
	//삭제 
	@DeleteMapping("/notice/{noticeid}/delete")
	public ResponseEntity noticedelete(@PathVariable long noticeid) throws IllegalAccessException {
		adminservice.deletenotice(noticeid);
		
		return ResponseEntity.ok("게시글삭제성공");
	}
	
	//=================================댓글페이지관리========================================
	
	@GetMapping("/commentmanage")
	public ResponseEntity commentmanage(@RequestParam(defaultValue="1") int page,
			@RequestParam(required = false) String option,
			@RequestParam(required =false) String searchtext) {
		if(searchtext==null) {
			Page<CommentDto> commentlist=adminservice.allCommentrget(page);
			return ResponseEntity.ok(commentlist);
		}else {
			Page<CommentDto> commentlist=adminservice.commentsearch(page,option,searchtext);
			
			return ResponseEntity.ok(commentlist);
		}
		
		
		 
	}
	
	//=================================채팅방페이지관리========================================
	@GetMapping("/chatroommanage")
	public ResponseEntity chtroommanage(@RequestParam(defaultValue="1") int page,
			@RequestParam(required = false) String option,
			@RequestParam(required =false) String searchtext) throws IllegalAccessException {
		
		if(searchtext==null) {
			Page<roomlistresponseDto> chatroomlist=adminservice.allRoomget(page);
			return ResponseEntity.ok(chatroomlist);
		}else {
			Page<roomlistresponseDto> chatroomlist=adminservice.searchrooms(page,option,searchtext);
			
			return ResponseEntity.ok(chatroomlist);
		}
		
		
		
	}
	
	
	//=================================멤버페이지관리========================================
	
	@DeleteMapping("/member/{userid}/delete")
	public ResponseEntity deletemember(@PathVariable("userid") Long userid) throws IllegalAccessException {
		System.out.println("유저삭제"+userid);
		String message=adminservice.memberdelete(userid);
		return ResponseEntity.ok(message);
	}
	
	
}
