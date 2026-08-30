package com.example.firstproject.controller;


import java.net.URI;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import com.example.firstproject.Service.Memberservice.EmailVerifyService;
import java.awt.PageAttributes.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.example.firstproject.Dto.userdataDto.ProfileUpdateDto;
import javax.validation.constraints.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.firstproject.CustomError.CustomException;
import com.example.firstproject.CustomError.ErrorCode;
import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.Memberform;
import com.example.firstproject.Dto.Weather.MemberUpdateDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Service.Memberservice.MemberService;
import com.example.firstproject.configure.PrincipalDetails;
import com.example.firstproject.tools.Userinfoheader;
import com.example.firstproject.configure.auth.authenticationfilter;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice.Return;


//클래스 레벨 @CrossOrigin(origins="https://localhost:3000") 이 붙어 있었다.
//프론트는 http://localhost:3001 이라 프로토콜·포트가 둘 다 달라 아무 오리진도 통과하지 못했고,
//클래스 레벨 설정이 전역 CORS 설정(securityconfig/WebConfig)을 덮어써서
//이 컨트롤러만 CORS 가 깨질 수 있었다. 오리진은 한 곳(app.cors.allowed-origins)에서만 정한다.
@RestController
@Slf4j
@Validated //패스배리어블검증용
public class MemberController {


	@Autowired
	MemberService memberservice;

	@Autowired
	EmailVerifyService emailverifyservice;

	//인증을 마친 사용자를 돌려보낼 프론트 주소(배포 시 APP_FRONTEND_URL).
	@Value("${app.frontend-url}")
	private String frontendurl;
	
	//로그인유지 임시 확실한지모름 엑세스토큰을써야하나?
	//폐기
	@GetMapping("/userdata")
	public void userdataget(Authentication authentication,HttpServletResponse res) {
		PrincipalDetails cipal=(PrincipalDetails) authentication.getPrincipal();

		//쿠키 이름이 "usernifo" 오타였고 값도 Map.toString() 이라 JSON 도 아니었다.
		//프론트에서 읽는 곳이 없다(폐기된 엔드포인트). 다른 곳과 같은 형식으로 통일해 둔다.
		Userinfoheader.write(res, cipal.getMember());
	}
	
	//멤버가입
	@PostMapping(value="/open/membercreate")
	public MemberDto Member(@Valid @RequestBody Memberform form) {
		log.info(form.toString());
		log.info("폼데이터 흠");
		log.info(form.getUsername().toString());
		
		MemberDto dto= memberservice.membercreate(form);
		return dto;
	}
	
	
	//가입인증링크 인증서비스!
	@GetMapping("/open/member/register")
	public ResponseEntity<Void> verifyemail(@RequestParam(required=false) String token) {

		EmailVerifyService.Result result=emailverifyservice.verify(token);
		log.info("이메일 인증 결과 {}", result);

		//예전에는 <script>alert(...)</script> 문자열을 그대로 돌려줬다.
		//  - @RestController 라 클라이언트에 따라 스크립트가 글자로 보일 수 있었고
		//  - alert 이었고, 돌아갈 주소에 localhost:3001 이 박혀 있었다.
		//302 로 보내면 토큰이 붙은 주소가 히스토리·Referer 에 남는 시간도 짧아진다.
		String to=frontendurl+"/login?verified="+result.name().toLowerCase();
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(to)).build();
	}

	/**
	 * 인증메일 재발송.
	 * 계정이 없든, 이미 인증됐든, 쿨다운에 걸렸든 <b>응답은 항상 같다.</b>
	 * 다르게 주면 이메일 존재 여부를 알아낼 수 있다.
	 */
	@PostMapping("/open/member/resend")
	public ResponseEntity<Map<String,String>> resendverifymail(@RequestBody Map<String,String> body) {
		emailverifyservice.resend(body==null?null:body.get("username"));
		return ResponseEntity.ok(Collections.singletonMap("message",
				"인증 메일을 다시 보냈습니다. 메일함을 확인해 주세요."));
	}
	
	//이메일중복확인
	@GetMapping(value="/open/emailcheck")
	public  ResponseEntity emailcheck(@RequestParam @Email String username) {
		log.info(username);//Validated랑 발리데이션어노테이션으로 가능함파람도 
		
		boolean check =memberservice.Emailauth(username);
		/*
		Map<String,Object> chemap=new HashMap<String,Object>();
		Long check = memberservice.findbyemail(username);
		int em=check.intValue();
		if(em==1) {
			chemap.put("check", check);
			
			log.info(chemap.toString());
			
			return chemap;
		}
		else {
			String authokey=memberservice.Emailauth(username);
			chemap.put("check", check);
			
			return chemap;
			}
			*/
		
		return ResponseEntity.ok(check);
		
		
	}
	//닉네임중복확인
	@GetMapping("/open/profileidcheck")
	public ResponseEntity dupliprofileidcheck(@RequestParam String profileid) {
		  if (profileid == null || profileid.trim().isEmpty()) {
		        throw new IllegalArgumentException("프로필 아이디를 입력해주세요.");
		    }
		    if (!profileid.matches("^[a-zA-Z가-힣0-9]{3,16}$")) {
		        throw new IllegalArgumentException("프로필 아이디는 3~16자의 한글, 영문, 숫자만 가능합니다.");
		    }
		    
		    
		   
		    boolean profileidcheck=memberservice.profileidcheck(profileid);
		    
		    return ResponseEntity.ok(profileidcheck);
		    
		 
	}
	
	//[삭제됨] POST /open/memberlogin
	//스프링시큐리티 도입전에 쓰던 수동 로그인 API. 프론트는 /login(authenticationfilter)만 사용한다.
	//- 평문 비밀번호와 BCrypt 해시를 equals 로 비교해서 애초에 동작하지 않았고
	//- 성공시 MemberEntity 를 통째로 응답에 실었으며(비밀번호 해시/리프레시토큰 노출)
	//- STATELESS 설정인데 세션을 생성했다.

	//비밀번호 찾기 임시비번발급
	@GetMapping("/open/passwordfind")
	public ResponseEntity passfind(@RequestParam String email) {
		
			Map<String,String> data=memberservice.passfind(email);
			//memberservice.memberpasswordupdate(email,authokey);
			
			return ResponseEntity.ok(data);		
	
		
		
	}
	
	//비밀번호와 닉네임 벽녕
	@PutMapping(value="/memberupdate/{email}")
	public String memberupdate(Authentication authentication,
			//@RequestBody HashMap<String,Object> data
			@Valid @RequestPart(value="dto",required = false) MemberUpdateDto dto
			,@RequestPart(required =false,value = "newprofile") MultipartFile newprofile,
			HttpServletResponse response
			) throws UnsupportedEncodingException {
		//String name=data.get("name").toString();
		//String password=data.get("password").toString();
		//getAuthorities() 는 권한 "목록"(UnmodifiableRandomAccessList)을 돌려준다.
		//로그인한 사용자 본체는 getPrincipal() 이다. 다른 컨트롤러도 모두 이쪽을 쓴다.
		PrincipalDetails user=(PrincipalDetails) authentication.getPrincipal();
		
		
		System.out.println("현재닉네임"+dto.getEmail());
	    System.out.println("현재닉네임"+dto.getName());
	    System.out.println("멤버주소"+dto.getRegion());
	    
	    System.out.println("현재프로파일"+dto.getProfileimage());
	    System.out.println("현재프로파일"+newprofile);
	   
	    MemberEntity member=new MemberEntity();
	    
	   if(newprofile !=null) {
	   String profileurl=memberservice.profileimagesave(newprofile, user.getUsername());
	   
	   log.info(profileurl);
	  member=memberservice.memberupdate(dto.getEmail(),dto, profileurl);
	   if(dto.getProfileimage() !=null) {
		   log.info("기존프로필이미지삭제");
		   memberservice.existingprofile(dto.getProfileimage());
	   }
	   
	   }else {
		 member=memberservice.memberupdate(dto.getEmail(),dto, dto.profileimage);
	   }
	   
	
	   //바뀐 정보를 화면에 바로 반영하기 위해 userinfo 를 다시 내려준다.
	   //쿠키가 아니라 응답 헤더다(tools/Userinfoheader 주석 참고).
	   //예전엔 여기서 만드는 JSON 에만 userid 가 빠져 있어서, 회원정보를 수정하고 나면
	   //userid 로 키를 만드는 화면들(채팅방·팔로우 목록)이 조용히 깨졌다.
	   Userinfoheader.write(response, member);
	 
	   
		//MemberEntity opdto=memberservice.findemail(email).orElseThrow();
	
			//Map<String,Object> map=new HashMap();
			//map.put("data", "success");맵안해도되네
			
	   return null;
		
		
	}
	
	/* 유저페이지 Edit Profile.
	   /memberupdate 를 쓰지 않는 이유는 ProfileUpdateDto 주석 참고
	   (그쪽은 지역을 조건 없이 덮어써서 이 화면에서 쓰면 주소가 날아간다). */
	@PutMapping(value="/profileupdate")
	public ResponseEntity<Object> profileupdate(Authentication authentication,
			@Valid @RequestPart(value="dto") ProfileUpdateDto dto,
			@RequestPart(required=false,value="newprofile") MultipartFile newprofile,
			@RequestPart(required=false,value="newbackground") MultipartFile newbackground,
			HttpServletResponse response) throws UnsupportedEncodingException {

		PrincipalDetails user=(PrincipalDetails) authentication.getPrincipal();

		//새 파일이 없으면 null 이 넘어가고, 서비스는 기존 이미지를 그대로 둔다
		String profileurl=memberservice.imagesave(newprofile,"userprofileimg");
		String backgroundurl=memberservice.imagesave(newbackground,"userbackgroundimg");

		MemberEntity member=memberservice.profileupdate(user.getUsername(),dto,profileurl,backgroundurl);

		//교체됐을 때만 옛 파일을 지운다
		if(profileurl!=null) {
			memberservice.imagedelete("userprofileimg",dto.getProfileimage());
		}
		if(backgroundurl!=null) {
			memberservice.imagedelete("userbackgroundimg",dto.getProfilebackground());
		}

		//바뀐 닉네임/프로필이 헤더에 바로 반영되도록 userinfo 를 다시 내려준다
		Userinfoheader.write(response, member);

		return ResponseEntity.ok().build();
	}

	//회원 탈퇴 코드 인증
	@PostMapping("/memberdeletemail")
	public String memberdeleteemail(@RequestBody HashMap<String,Object> data) {
		log.info("멤버삭제이메일보내기!");
		String email=data.get("email").toString();
		String deletecode=memberservice.deletecodesend(email);
		log.info("성공적");
		return deletecode;
	
	}
	
	@DeleteMapping("/memberdelete")
	public void memberdelete(@RequestBody Map<String,Object> data) {
		log.info("멤버딜리트시도!"+data.get("username"));
		log.info("authkey:"+data.get("authkey"));
		
		memberservice.deletemember(data.get("username").toString(),data.get("authkey").toString());
	}
	
	
	
	
	//폐기 jwt이후로쓸필요음 세션으로 할때 연습용
	@GetMapping("/open/logincheck")
	public String asdf(HttpServletRequest req,HttpServletResponse res) {
		log.info("로그인첵");
		HttpSession session = req.getSession(false);//세션이있으면 넣고 없으면 NULL true하면 새로생성
		
		
		ResponseCookie rescookie=ResponseCookie.from("as", "ssd")
				.path("/")
				.sameSite("None")
				.httpOnly(false)
				.secure(true)
				.maxAge(60*20*1800)
				.build();
		res.addHeader("Set-Cookie",rescookie.toString());
		
		if(session!=null &&session.getAttribute("loginmember")==null) {
			
			return "없음세션";
		}else {
		
			
			 /*Cookie cookie=new Cookie("member","실허용");
			 cookie.setMaxAge(60*40);
			cookie.setPath("/");
			cookie.setSecure(true);
			cookie.setHttpOnly(false);
			
			res.addCookie(cookie);*/
		
		log.info(rescookie.toString());
		
		return "있음";
		}
	
	}
	//멤버아이디차직
	@GetMapping("/open/usernamefind/{username}")
	public ResponseEntity Usernamefind(@PathVariable String username) {
		
		
		Map<String, String> data=memberservice.Usernamefind(username);
	
		return ResponseEntity.ok(data);
	}


	



}
