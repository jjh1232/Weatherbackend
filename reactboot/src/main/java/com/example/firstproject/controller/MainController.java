package com.example.firstproject.controller;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.NoticeUpdate;
import com.example.firstproject.Dto.Noticeform;
import com.example.firstproject.Dto.detachVo;
import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Dto.Comment.Commentform;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Repository.NoticeRepository;
import com.example.firstproject.Service.NoticeService;
import com.example.firstproject.Service.Memberservice.MemberService;
import com.example.firstproject.configure.PrincipalDetails;

import lombok.extern.slf4j.Slf4j;

//@CrossOrigin(origins ="*",exposedHeaders = "*")
@RestController
@Slf4j
public class MainController {

	@Autowired
	private NoticeRepository repoo;
	@Autowired
	private NoticeService noticeservice;
	
	@Autowired
	private MemberService memberservice;
	

	
	@GetMapping("/ex")
	public List<NoticeDto> aasssd(@RequestParam(value="page",required =false,defaultValue="1") int page){
		//Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"red"));
		
		List<NoticeDto> noticedto = noticeservice.readfd(page);
		
		return noticedto;
	}
	@GetMapping("/open/count")
	public long cou() {
		long count=repoo.count();//기본메서드
		
		 
		return count;
	}
	
	//이거 지금 안씀 여기서 데이터정리하는게 나은거같아서
	@GetMapping("/open/notice")
	public Page<NoticeDto> notice(@RequestParam(value="page",required =false,defaultValue="1") int page) {
		System.out.println("페이지"+page);
		//List<NoticeDto> notice = new ArrayList<NoticeDto>();
		Pageable pageable =PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"red"));
		Page<NoticeDto> notice=noticeservice.read(pageable);
		
		
		return notice;
	}
	
	
	//이게지금 메인트윗으로쓰고있음
	@GetMapping("/open/noticesearch")
	public Page<NoticeDto> search(@RequestParam(required = false,defaultValue="title") String option,
			@RequestParam(required = false,defaultValue="") String keyword,
			@RequestParam(defaultValue="1") int page) {
		//noticeservice.search(option,keyword);
		if(keyword.equals("")) {
			log.info("키워드널 검색어가아님");	
			Pageable pageable =PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"red"));
			Page<NoticeDto> Dto=noticeservice.read(pageable);
			return Dto;
		}else {
			log.info("검색임 ");	
			log.info("페이지"+page);
		log.info(option);
		log.info(keyword);
		log.info(String.valueOf(page));
		Page<NoticeDto> Dto=noticeservice.search(option,keyword,page);
		//리퀘랑페이지똑같넹 ;
		return Dto;
		}
		
	}
	
	//=====================================일단 좋아요 목록가져오는컨트롤러 ============================================
	@GetMapping("/onlikenotice")
	public ResponseEntity search(Authentication authentication,@RequestParam(defaultValue="1") int page){
		PrincipalDetails principal=(PrincipalDetails) authentication.getPrincipal();
		Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createat"));
		System.out.println("들어온페이지:"+page);
		Map<String,Object> dto=noticeservice.favoritenotice(principal.getMember(),pageable);
		
		
		
		return ResponseEntity.ok(dto);
	}

	
	
	
	@PostMapping(value="/noticecreate")
	public void create(@Validated @RequestBody Noticeform form) {//리퀘스트바디와 겟터셋터필수임;
		System.out.println("게시글작성!");
		noticeservice.noticecreate(form);
		System.out.println("컨트롤러크레딧");
		
	}
	//=============디테일
	@GetMapping("/open/noticedetail/{num}")
	public NoticeDto noticedetail(@PathVariable Long num) {
		log.info("글들");
		NoticeDto Dto =noticeservice.detail(num);
		System.out.println(Dto);
		return Dto;
	}
	//=====================수정검사==========================================
	@GetMapping("/noticeupdate/{num}")
	public NoticeDto noticeupdatedetail(@PathVariable Long num,Authentication authentication) throws Exception {
		PrincipalDetails principal=(PrincipalDetails) authentication.getPrincipal();
		String username=principal.getUsername();
		NoticeDto dto=noticeservice.detail(num);
		if(username.equals(dto.getUsername())) {
			log.info("유저가일치합니다!");
			return dto;
		}
		else {
			log.info("유저가 일치하지않아요 로그인정보를확인해주세요");
			throw new Exception("아이디불일치");
		}
		
		
	}
	
	//=======================삭제===============================
	@DeleteMapping("/noticedelete/{num}")
	public void delete(@PathVariable Long num,Authentication authentication) throws Exception {
		PrincipalDetails principal=(PrincipalDetails) authentication.getPrincipal();
		String username=principal.getUsername();
		NoticeDto dto=noticeservice.detail(num);
		if(username.equals(dto.getUsername())) {
			log.info("유저가일치합니다!");
			noticeservice.delete(num);
		}
		else {
			log.info("유저가 일치하지않아요 로그인정보를확인해주세요");
			throw new Exception("아이디불일치");
		}
		
		
	}
	@PutMapping("/noticeupdate/{num}")
	public NoticeDto update(@PathVariable Long num,@Validated @RequestBody NoticeUpdate update) {
		NoticeDto dto=noticeservice.noticeupdate(num,update);
		return dto;
	}
	//==================코멘트생성========================================
	@PostMapping("/commentcreate")
	public void comment(@RequestBody Commentform form ) {
		System.out.println("댓글작성");
		System.out.println(form.toString());
		noticeservice.Commentcreate(form);
		System.out.println("댓글알람성공시 작성");
		/*
		System.out.println(form.getNoticeid());
		//noticeservice.sendalarm(form.getNoticeid(),form.getNoticenum(),form.getNoticetitle());
		//Long notifiuserid=memberservice.findbyemail(form.getNoticeid());
		Long userid=memberservice.findbyemail(form.getUsername());
		System.out.println("받을아이디:"+form.getNoticeid()+"보낼아이디:"+userid);
		memberservice.sendto(form.getNoticeid(),userid);
		*/
	}
	
	//코멘트 관련
	@GetMapping("/open/comment/{num}")
	public List<CommentDto> comments(@PathVariable int num) {
		
		
		List<CommentDto> dto=noticeservice.commentget(num);
		if(dto==null) {
			log.info("댓글없음");
			return null;
		}
		else {
			log.info("댓글있음");
			return dto;
		}
		
	}
	
	
	@GetMapping("/open/extest")
	public Page<NoticeEntity> exsearch(@RequestParam String option,@RequestParam String keyword,
			@RequestParam(defaultValue="1") int page
			){
		Pageable Pageable=PageRequest.of(page-1,10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"red"));
		
		Page<NoticeEntity> entity =repoo.searchnoticeex(keyword,Pageable);
		
		return entity;
	}
	
	@PutMapping("/commentupdate")
	public void commentupdate(@RequestBody HashMap<String,Object> updatedata) {
		Long id=Long.valueOf(updatedata.get("id").toString());
		String email=updatedata.get("username").toString();
		String text=updatedata.get("text").toString();
		
		noticeservice.commentupdate(id,email,text);
		
	}
	
	@DeleteMapping("/commentdelete/{id}")
	public void commentdelete(@PathVariable Long id) {
		System.out.println("댓글삭제!");
		noticeservice.commentdelete(id);
	}


	@GetMapping("/usertest")
	public String usertest(Authentication authentication) {
		System.out.println("유저테스트사이트");
		
		PrincipalDetails cipal=(PrincipalDetails) authentication.getPrincipal();
		String username=cipal.getUsername();
		System.out.println(cipal.toString());
		System.out.println(cipal.getAuthorities());
	
		MemberEntity member=cipal.getMember();
		return member.toString();
	}
	

	//이미지데이터 저장 
	@PostMapping("/contentimage")
	public String imagesave(MultipartFile image) {
		System.out.println("들어온이미지:"+image);
		String path=noticeservice.contentimagesave(image);
		log.info("리액트패스경로확인용:"+path);
		return path;
	}
	//이미지데이터삭제
	@DeleteMapping("/deletecontentimage")
	public void saveimagecut(@RequestBody Map<String,String> detach) {
		log.info(detach.get("path"));
		//파일데이터삭제만했는데 Db의파일도 삭제해줘야함 
		noticeservice.saveimagecut(detach.get("id"),detach.get("path"));
		
		
	}
	//이미지데이터 첨부파일
	@PostMapping("/open/getdetach")
	public ResponseEntity detachget(@RequestBody detachVo detach) {
		log.info("detach파일내임"+detach.getFilename());
		log.info("detachurl"+detach.getUri());
		return noticeservice.getdetach(detach);
		
	}
	
	@GetMapping("/open/atagdown") //a태그는 스프링부트에서막는다.. 
	public ResponseEntity atagdown(@RequestParam String path) {
		log.info(path);
		Path filepath = Paths.get("D:/study프로그램/react/bootproject/public"+path);
		try {
			UrlResource resource=new UrlResource(filepath.toUri());
			//한글파일이름 꺠질수있으니인코딩
			String encodeupload=UriUtils.encode("파일",StandardCharsets.UTF_8);
			String contentDisposition = "attachment; filename=\"" + encodeupload + "\"";
			
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION,contentDisposition)
					.body(resource);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			log.info("오류!");
			e.printStackTrace();
		}
		return null;
	}
	
	//==================================게시글좋아요기능 =================================
	@GetMapping("/noticelike/{noticeid}")
	public ResponseEntity<Object> likenotice(@PathVariable Long noticeid,Authentication authentication ) {
		PrincipalDetails detailes=(PrincipalDetails) authentication.getPrincipal();
		//MemberEntity member=detailes.getMember();
		log.info("게시판좋아요컨트롤러"+detailes.getMember().getUsername());
		ResponseEntity message=noticeservice.noticelikes(detailes.getMember(), noticeid);
		
		
		return message;
		
	}
	
	//=================좋아요 체크해봄=====================================
	@GetMapping("/noticelikecheck/{noticeid}")
	public ResponseEntity<Object> noticelikecheck(@PathVariable Long noticeid,Authentication authentication ) {
		PrincipalDetails detailes=(PrincipalDetails) authentication.getPrincipal();
		boolean like= noticeservice.noticelikecheck(detailes.getMember(), noticeid);
		
		return ResponseEntity.ok(like);
	}
	
}
