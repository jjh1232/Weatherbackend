package com.example.firstproject.configure.auth;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
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

import com.nimbusds.jose.util.StandardCharset;

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
			
			json.put("userid",principal.getid());
			json.put("username",principal.getUsername());
		
			json.put("nickname",principal.getMember().getNickname());
			
			json.put("region", principal.getMember().getHomeaddress().getJuso());
			json.put("gridx", principal.getMember().getHomeaddress().getGridx());
			json.put("gridy", principal.getMember().getHomeaddress().getGridy());
			json.put("profileimg", principal.getMember().getProfileimg());
			json.put("userrole", principal.getMember().getRole());
			json.put("Profileid",principal.getMember().getProviderid());
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
	    
	    System.out.println("로그인전경로로하기위해체크:"+request.getParameter("state"));
	    String prevPath = request.getParameter("state");
	    String decode=URLDecoder.decode(prevPath,StandardCharset.UTF_8.name());
	    System.out.println("디코드값:"+decode);
	    
	    boolean isTemp = authentication.getAuthorities().stream()
	            .anyMatch(auth -> "ROLE_TEMP".equals(auth.getAuthority())); //권한가져오기
	    		//anyMatch는 요소중 조건을만족하는요소가있는지찾는것
	    if (isTemp) {
	    	System.out.println("템프유저");
	        response.sendRedirect("http://localhost:3001/signup/extrainfo");
	        return;
	    }
		 //이거구해놓고왜저기세션을썻지..의아
	    System.out.println("정식유저");
	     response.sendRedirect("http://localhost:3001/oauthsuccess");
	}

}
