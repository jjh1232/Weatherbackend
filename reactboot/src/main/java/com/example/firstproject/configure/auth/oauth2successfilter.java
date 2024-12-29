package com.example.firstproject.configure.auth;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.LoginHistory;
import com.example.firstproject.Service.JwtService;
import com.example.firstproject.Service.Memberservice.HistoryService;
import com.example.firstproject.configure.PrincipalDetails;

import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;


@Service
@RequiredArgsConstructor
public class oauth2successfilter implements AuthenticationSuccessHandler{

	@Autowired
	JwtService jwtservice;
	
	private final HistoryService historyservice;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		System.out.println("oauth2로그인석세스");
		 System.out.println("석세스핸들러이건머지:"+authentication.getAuthorities().stream().map(Object::toString).collect(Collectors.joining(" ")));
		
		 PrincipalDetails principal=(PrincipalDetails) authentication.getPrincipal();
		 
		 System.out.println("어트리뷰트스:"+principal.getMember());
	      System.out.println("어트리뷰트스:"+principal.getUsername());
	      System.out.println("어트리뷰트스:"+ principal.getAuthorities());
	      
	      String jwttoken=jwtservice.createtoken(principal);
	      String refreshtoken=jwtservice.createrefreshtoken();
	      jwtservice.Setrefreshtoken(principal.getUsername(), refreshtoken);
	      
	      System.out.println("jwt토큰"+jwttoken);
	      System.out.println("리프레쉬토큰토큰"+refreshtoken);
	      
	      
	      //response.addHeader("Authorization", jwttoken);
	      //response.addHeader("Refreshtoken", refreshtoken);
	      
	   
	    //json형태로 쿠키에 여러값의 유저인포저장!
			JSONObject json= new JSONObject();
			
			json.put("username",principal.getUsername());
		
			json.put("nickname",principal.getMember().getNickname());
			
			json.put("region", principal.getMember().getHomeaddress());
			json.put("profileimg", principal.getMember().getProfileimg());
			//쿠키에 = 등의기호와 한글은 저장안되기때문에 URLEncoder사용해서 저장
			   Cookie cookie1=new Cookie("Acesstoken",jwttoken);
			      Cookie cookie2=new Cookie("Refreshtoken",refreshtoken);
			Cookie idCookie=new Cookie("userinfo",URLEncoder.encode(json.toJSONString(),"UTF-8"));
			cookie1.setPath("/");
			cookie2.setPath("/");
			idCookie.setPath("/");//사용가능한패스
	      response.addCookie(cookie1);
	     response.addCookie(cookie2);
	     response.addCookie(idCookie);
	     String clientip=historyservice.getrequestIp(request);
	     LoginHistory history=LoginHistory.builder()
	    		 .userid(principal.getUsername())
					.islogin(true)
					.clientip(clientip) //이부분좀봐야할듯
					.userdata(request.getLocale().toString())
					.build();
	     historyservice.saveLoginlog(history);
	     System.out.println("리다이렉트체크:"+request.getHeaders("Referer"));
	    
	    
	     response.sendRedirect("http://localhost:3001/main");
	}

}
