package com.example.firstproject.admin;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.firstproject.Dto.Admindetachchangeform;
import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.ChatDto.AdminroomdetailDto;
import com.example.firstproject.Dto.ChatDto.roomlistresponseDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.Roomdata;
import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Dto.Comment.Commentform;
import com.example.firstproject.Dto.blockDto.Adminnoticedecleresponsedto;
import com.example.firstproject.Dto.userdataDto.LoginHistoryDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Service.Blockservice.Blockservice;
import com.example.firstproject.admin.form.Admemberupdateform;
import com.example.firstproject.admin.form.Adminmembercreateform;
import com.example.firstproject.admin.form.AdminnoticeUpdateform;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class admincontroller {

	private final adminService adminservice;
	
	private final Blockservice blockservice;
	
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
	//회원 로그인기록 확인하기
	@GetMapping("/loginhistory")
	public ResponseEntity loginhistoryget(@RequestParam String username,
										  @RequestParam(defaultValue = "1") int page,
										  @RequestParam(defaultValue = "novalue") String year,
										  @RequestParam(defaultValue = "novalue") String month,
										  @RequestParam(defaultValue = "false") boolean isasc
			) {
		
		Page<LoginHistoryDto> dtolist=adminservice.finduserhistory(username,page,year,month,isasc);
		
		return ResponseEntity.ok(dtolist);
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
		System.out.println("노티스디테일찾기");
		NoticeDto dto=adminservice.noticedetail(noticeid);
		
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
	
	@GetMapping("/notice/detail/{noticeid}")
	public ResponseEntity noticedetail(@PathVariable Long noticeid) throws IllegalAccessException {
		NoticeDto dto=adminservice.noticedetail(noticeid);
		return ResponseEntity.ok(dto);
	}
	//게시글이미지 변경
	@PutMapping("/imageban/{detachid}")
	public ResponseEntity detachchange(@PathVariable Long detachid) throws IllegalAccessException {
		Long imageid=adminservice.changeimage(detachid);
		return ResponseEntity.ok(imageid+"가잘변경되었습니다");
	};
	//게시글이미지 차단해제 (오차단 되돌리기)
	@PutMapping("/imagerestore/{detachid}")
	public ResponseEntity detachrestore(@PathVariable Long detachid) throws IllegalAccessException {
		Long imageid=adminservice.restoreimage(detachid);
		return ResponseEntity.ok(imageid+"번 이미지를 되돌렸습니다");
	}
	//게시글이미지 단체변경
	@PutMapping("/manyimageban")
	public ResponseEntity manyimagechange(@RequestBody Admindetachchangeform form) throws IllegalAccessException {
		System.out.println(form.getDetachids());
		adminservice.manychangeimage(form);
		
		return ResponseEntity.ok("성공");
	}
	
	//게시판 신고 정보 가져오기===============================================================
	@GetMapping("/noticedecle/{noticeid}")
	public ResponseEntity decleget(@PathVariable Long noticeid
			,@RequestParam(name = "page",defaultValue = "1") int page) {
		System.out.println("페이지:"+page);
		Page<Adminnoticedecleresponsedto> list=blockservice.noticedecledata(noticeid, page);
		
		return ResponseEntity.ok(list);
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
	/* [삭제됨] 댓글 업데이트 (@PutMapping("/commentupdate/{commentid}"))
	   운영자가 남의 댓글 내용을 고치면 작성자 이름으로 안 쓴 말이 남는다.
	   게다가 이 폼은 username/nickname 까지 바꿀 수 있어 작성자를 갈아끼울 수 있었다.
	   부적절한 내용을 가리는 건 차단(commentblock)이 맡는다.
	   차단은 원문을 지우지 않아 되돌릴 수 있고, 조치 사실도 화면에 남는다. */
	
	//댓글 운영자 차단 / 해제
	@PutMapping("/commentblock/{commentid}")
	public ResponseEntity commentblock(@PathVariable Long commentid) throws IllegalAccessException {
		adminservice.commentblock(commentid);
		return ResponseEntity.ok("차단되었습니다");
	}
	@PutMapping("/commentunblock/{commentid}")
	public ResponseEntity commentunblock(@PathVariable Long commentid) throws IllegalAccessException {
		adminservice.commentunblock(commentid);
		return ResponseEntity.ok("차단이 해제되었습니다");
	}

	@DeleteMapping("/commentdelete/{commentid}")
	public ResponseEntity commentdelete(@PathVariable Long commentid) throws IllegalAccessException {
		adminservice.commentdelete(commentid);
		return ResponseEntity.ok("삭제되었습니다!");
		
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
	//채팅방삭제
	@DeleteMapping("/roomdelete/{roomid}")
	public ResponseEntity roomdelete(@PathVariable Long roomid) throws IllegalAccessException {
		Long getroomid=adminservice.roomdelete(roomid);
		
		return ResponseEntity.ok(getroomid+"번방 삭제되었습니다");
	}
	
	
	//채팅방들어가기
	@GetMapping("/room/{roomid}")
	public ResponseEntity roomdetail(@PathVariable Long roomid) throws IllegalAccessException {
		System.out.println("채팅방디테일들어가기on");
		Roomdata dto=adminservice.roomdetail(roomid);
		
		
		return ResponseEntity.ok(dto);
		
	}
	//=================================멤버페이지관리========================================
	
	@DeleteMapping("/member/{userid}/delete")
	public ResponseEntity deletemember(@PathVariable("userid") Long userid) throws IllegalAccessException {
		System.out.println("유저삭제"+userid);
		String message=adminservice.memberdelete(userid);
		return ResponseEntity.ok(message);
	}
	
	
}
