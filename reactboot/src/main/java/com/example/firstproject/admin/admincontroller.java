package com.example.firstproject.admin;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.ChatDto.roomlistresponseDto;
import com.example.firstproject.Entity.MemberEntity;

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
	public ResponseEntity memberpage(@RequestParam(defaultValue = "1") int page) {
		
		System.out.println("어드민시작");
		Page<MemberDto> memberlist=adminservice.allmemberget(page);
		System.out.println("데이터전달");
		
		return ResponseEntity.ok(memberlist);
		
	}
	//=================================게시판페이지관리========================================
	@GetMapping("/noticemanage")
	public ResponseEntity noticemanage(@RequestParam(defaultValue="1") int page,
			@RequestParam(required = false) String option,
			@RequestParam(required =false) String searchtext
			) {
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
	
	//삭제 
	@DeleteMapping("/notice/{noticeid}/delete")
	public ResponseEntity noticedelete(@PathVariable long noticeid) throws IllegalAccessException {
		adminservice.deletenotice(noticeid);
		
		return ResponseEntity.ok("게시글삭제성공");
	}
	
	//=================================댓글페이지관리========================================
	@GetMapping("/commentmanage")
	public ResponseEntity commentmanage(@RequestParam(defaultValue="1") int page) {
		//Page<NoticeDto> memberlist=adminservice.allCommentrget(page);
		
		 return null;
	}
	
	//=================================채팅방페이지관리========================================
	@GetMapping("/chatroommanage")
	public ResponseEntity chtroommanage(@RequestParam(defaultValue="1") int page) {
		Page<roomlistresponseDto> memberlist=adminservice.allRoomget(page);
		
		 return ResponseEntity.ok(memberlist);
	}
	
	
	//=================================멤버페이지관리========================================
	
	@DeleteMapping("/member/{userid}/delete")
	public ResponseEntity deletemember(@PathVariable("userid") Long userid) throws IllegalAccessException {
		System.out.println("유저삭제"+userid);
		String message=adminservice.memberdelete(userid);
		return ResponseEntity.ok(message);
	}
	
	
}
