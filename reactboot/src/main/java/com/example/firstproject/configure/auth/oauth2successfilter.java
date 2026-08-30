package com.example.firstproject.configure.auth;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.LoginHistory;
import com.example.firstproject.Service.JwtService;
import com.example.firstproject.Service.Memberservice.HistoryService;
import com.example.firstproject.configure.PrincipalDetails;
import com.example.firstproject.tools.ClientIp;
import com.example.firstproject.tools.Userinfoheader;


import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;


@Service
@RequiredArgsConstructor
public class oauth2successfilter implements AuthenticationSuccessHandler{

	@Autowired
	JwtService jwtservice;
	
	private final HistoryService historyservice;

	//로그인을 마친 사용자를 돌려보낼 프론트 주소. 배포 시 APP_FRONTEND_URL 로 덮어쓴다.
	@Value("${app.frontend-url}")
	private String frontendurl;
	
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
	      
	   
	    //프론트 화면 표시용 유저 정보. 프래그먼트에 실어 보낸다.
	    //내용은 일반 로그인과 같아야 하므로 만드는 곳도 한 곳(tools/Userinfoheader)으로 모았다.
			JSONObject json = Userinfoheader.of(principal.getMember());
			//=========================================================
		//토큰을 쿠키에 심지 않는다.
		//
		//쿠키는 "심은 쪽 도메인"의 소유가 된다. 로컬은 8081 과 3001 이 둘 다
		//localhost 라(쿠키는 포트를 구분하지 않는다) 프론트가 읽을 수 있었지만,
		//배포해서 api.도메인 과 프론트 도메인으로 갈라지면 프론트의
		//document.cookie 에서 아예 보이지 않는다. 로그인은 성공하는데
		//앱은 누가 로그인했는지 모르는 상태가 된다.
		//
		//그래서 리다이렉트 주소의 프래그먼트(# 뒤)에 실어 보낸다.
		//프래그먼트는 서버로 전송되지 않으므로 서버 로그·Referer 에 남지 않는다.
		//(쿼리스트링 ?token= 으로 보내면 그 전부에 남는다.)
		//프론트는 이 값을 저장한 직후 주소창에서 지운다.
		//
		//TODO 프론트·백엔드를 같은 상위 도메인의 서브도메인으로 맞추면
		//     refresh 토큰은 HttpOnly; Secure; SameSite=None 쿠키로 옮기는 것이 더 안전하다.
		//     그때 바꿀 곳은 이 메서드 하나뿐이다.
		//=========================================================
		String fragment = "#token="    + URLEncoder.encode(jwttoken, "UTF-8")
		                + "&refresh="  + URLEncoder.encode(refreshtoken, "UTF-8")
		                + "&userinfo=" + URLEncoder.encode(json.toJSONString(), "UTF-8");

		String clientip=ClientIp.resolve(request);
		LoginHistory history=LoginHistory.builder()
				.userid(principal.getUsername())
				.islogin(true)
				.clientip(clientip)
				.userdata(request.getLocale().toString())
				.build();
		historyservice.saveLoginlog(history);

		/* 예전엔 여기서 request.getParameter("state") 로 "로그인 전 경로"를 읽으려 했다.
		   그런데 state 는 스프링 시큐리티가 CSRF 방어용으로 직접 생성해서 넣고
		   콜백에서 검증하는 값이라, 프론트가 붙여 보낸 값은 덮어써진다.
		   즉 여기서 읽히는 건 경로가 아니라 시큐리티의 랜덤 문자열이었고,
		   읽어봐야 쓰는 곳도 없었다(게다가 state 가 없으면 NPE 로 터졌다).
		   돌아갈 경로는 프론트가 localStorage("oauthbeforepath") 로 들고 있다. */

		boolean isTemp = authentication.getAuthorities().stream()
				.anyMatch(auth -> "ROLE_TEMP".equals(auth.getAuthority()));

		//추가정보가 필요한 신규 가입자와 정식 유저의 도착지만 다르다.
		//토큰은 어느 쪽이든 넘겨야 한다(추가정보 저장도 인증이 필요하다).
		String target = isTemp ? "/signup/extrainfo" : "/oauthsuccess";
		System.out.println(isTemp ? "템프유저" : "정식유저");

		response.sendRedirect(frontendurl + target + fragment);
	}

}
