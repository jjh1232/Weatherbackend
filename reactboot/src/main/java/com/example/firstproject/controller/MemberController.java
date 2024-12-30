package com.example.firstproject.controller;


import java.awt.PageAttributes.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.Memberform;
import com.example.firstproject.Dto.Weather.MemberUpdateDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Service.Memberservice.MemberService;
import com.example.firstproject.configure.PrincipalDetails;
import com.example.firstproject.configure.auth.authenticationfilter;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;


@CrossOrigin(origins ="https://localhost:3000")
@RestController
@Slf4j
@Validated //패스배리어블검증용
public class MemberController {


	@Autowired
	MemberService memberservice;
	
	//로그인유지 임시 확실한지모름 엑세스토큰을써야하나?
	//폐기
	@GetMapping("/userdata")
	public void userdataget(Authentication authentication,HttpServletResponse res) {
		PrincipalDetails cipal=(PrincipalDetails) authentication.getPrincipal();
		String username=cipal.getUsername();
		String nickname=cipal.getMember().getNickname();
		String name=cipal.getName();
		Map<String,String> userinfo=new HashMap<>();
		userinfo.put("username", username);
		userinfo.put("nickname", nickname);
		userinfo.put("name", name);
		
		Cookie cookie=new Cookie("usernifo",userinfo.toString());
		
		res.addCookie(cookie);
		
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
	public String authkey(@RequestParam String username,@RequestParam String authokey) {
		
		int check=memberservice.auth(username,authokey);
		
		if(check==0) {
			System.out.println("인증성공");
			
			return "<script>"+"alert(\"이메일인증이완료되었습니다\");"
					+"location.href=\"http://localhost:3001/main/\";"
			+"</script>";
			}
		else {
			System.out.println("인증실패");
			return "<script>"+"alert(\"잘못된 인증메일입니다다시확인해주세요!\");"
			+"location.href=\"http://localhost:3001/main/\";"
	+"</script>";
		}
		
		
	}
	
	//이메일중복확인
	@GetMapping(value="/open/emailcheck")
	public  Map<String,Object> emailcheck(@RequestParam @Email String username) {
		log.info(username);//Validated랑 발리데이션어노테이션으로 가능함파람도 
		
		
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
		
		
		
	}
	
	//스프링시큐리티쓸꺼라 이제안쓸듯? 
	@PostMapping("/open/memberlogin")
	public Map<String,Object> memberlogin(
			@RequestBody HashMap<String,Object> reqjsonhashmap
			,HttpServletResponse res
			,HttpServletRequest req) {	
		
		//responseData
		
		//ArrayList<HashMap<String,Object>> data=new ArrayList<HashMap<String,Object>>();
		/* HttpServletRequest req 이걸로 데이터받아서 함 form data형식 
		 * HashMap<String,Object> ddata=new HashMap<String,Object>();
		 * ddata.put("RequestData1", req.getParameter("email"));
		 * ddata.put("RequestData2", req.getParameter("password"));
		 * log.info(ddata.toString());
		 */
		
		//response Data
		
		HashMap<String, Object> output = new HashMap<String, Object>();
//		rtnMap.put("data", reqjsonhashmap.get("password"));      
		String email = reqjsonhashmap.get("email").toString();
		String password = reqjsonhashmap.get("password").toString();
		
		Object result=memberservice.memberlogin(email,password);
		if(result == null) {
			output.put("result", "x");
			log.info(output.toString());
			return output;
		}
		else if(result.equals("x")) {
			output.put("result","notpass");
			log.info(output.toString());
			return output;
		}
		else {
			MemberEntity loginmem=(MemberEntity) result;
			output.put("member", loginmem);
			
			
			 //쿠키
			 Cookie cookie=new Cookie("member",loginmem.getNickname());
			 cookie.setMaxAge(60*40);
			cookie.setPath("/");
			cookie.setSecure(true);
			cookie.setHttpOnly(false);
			
			res.addCookie(cookie);
			//리스폰스헤더에 쿠키닮기 same사이트도되는데 프록시쓰니까위에꺼도되서안쓸
			
			
			
			// res.setHeader("Set-Cookie", responseCookie.toString()) ;
			  
			//  res.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString() );
		
			
			HttpSession session = req.getSession(true);//세션이이미있으면반환
			session.setAttribute("loginmember", loginmem);
			session.setMaxInactiveInterval(3600);
			log.info(session.getAttribute("loginmember").toString());
			
			return output;
	}}

	
	//비밀번호 찾기 임시비번발급 
	@GetMapping("/open/passwordfind")
	public String passfind(@RequestParam String email) {
		Long check=memberservice.findbyemail(email);
		if(check==0) {
			return "x";
		}
		else {
			String authokey=memberservice.passfind(email);
			memberservice.memberpasswordupdate(email,authokey);
			return "o";
		}
	
		
		
	}
	
	//비밀번호와 닉네임 벽녕
	@PutMapping(value="/memberupdate/{email}")
	public String memberupdate(@PathVariable String email,
			//@RequestBody HashMap<String,Object> data
			@Valid @RequestPart(value="dto",required = false) MemberUpdateDto dto
			,@RequestPart(required =false,value = "newprofile") MultipartFile newprofile,
			HttpServletResponse response
			) throws UnsupportedEncodingException {
		//String name=data.get("name").toString();
		//String password=data.get("password").toString();
		
		
		System.out.println("현재닉네임"+dto.getEmail());
	    System.out.println("현재닉네임"+dto.getName());
	    System.out.println("멤버주소"+dto.getRegion());
	    
	    System.out.println("현재프로파일"+dto.getProfileimage());
	    System.out.println("현재프로파일"+newprofile);
	   
	    MemberEntity member=new MemberEntity();
	    
	   if(newprofile !=null) {
	   String profileurl=memberservice.profileimagesave(newprofile, email);
	   
	   log.info(profileurl);
	  member=memberservice.memberupdate(dto.getEmail(),dto, profileurl);
	   if(dto.getProfileimage() !=null) {
		   log.info("기존프로필이미지삭제");
		   memberservice.existingprofile(dto.getProfileimage());
	   }
	   
	   }else {
		 member=memberservice.memberupdate(dto.getEmail(),dto, dto.profileimage);
	   }
	   
	   //새유저인포쿠키
	   JSONObject json= new JSONObject();
		
		json.put("username",member.getUsername());
	
		json.put("nickname",member.getNickname());
		
		json.put("region", member.getHomeaddress().getJuso());
		json.put("gridx", member.getHomeaddress().getGridx());
		json.put("gridy", member.getHomeaddress().getGridy());
		json.put("profileimg", member.getProfileimg());
		
	   Cookie idCookie=new Cookie("userinfo",URLEncoder.encode(json.toJSONString(),"UTF-8"));
		
		idCookie.setPath("/");//사용가능한패스
    
    response.addCookie(idCookie);
	 
	   
		//MemberEntity opdto=memberservice.findemail(email).orElseThrow();
	
			//Map<String,Object> map=new HashMap();
			//map.put("data", "success");맵안해도되네
			
	   return null;
		
		
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
	


	



}
