package com.example.firstproject.configure.auth;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.LoginHistory;
import com.example.firstproject.Service.Memberservice.HistoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class oauth2failservice implements AuthenticationFailureHandler{

	private final HistoryService historyservice;
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		// TODO Auto-generated method stub
		
		System.out.println("익셉션:"+exception.toString());
		System.out.println("oauth2로그인실패!매세지"+exception.getMessage());
		
		/* 히스토리 리퀘스트를 여기서만질수가없음...구글에서해서 이건포기
		String clientip=historyservice.getrequestIp(request);
	     LoginHistory history=LoginHistory.builder()
	    		 .userid(principal.getUsername())
					.islogin(true)
					.clientip(clientip) //이부분좀봐야할듯
					.userdata(request.getLocale().toString())
					.build();
	     historyservice.saveLoginlog(history);
		*/
		if(exception instanceof OAuth2AuthenticationException){
			System.out.println("이미사이트에가입한메일");
			String message="sameemail";
			response.sendRedirect("http://localhost:3001/oauth2loginfailed?msg="+message);
		}
		else {
			System.out.println("해당사이트서버문제?");
		response.sendRedirect("http://localhost:3001/oauth2loginfailed");
		}
	}

}
