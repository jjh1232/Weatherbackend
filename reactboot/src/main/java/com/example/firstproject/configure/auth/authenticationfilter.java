package com.example.firstproject.configure.auth;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.example.firstproject.CustomError.CustomResponseUtil;
import com.example.firstproject.Entity.LoginHistory;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Service.JwtService;
import com.example.firstproject.Service.Memberservice.HistoryService;
import com.example.firstproject.configure.PrincipalDetails;
import com.example.firstproject.tools.HttpResRequestWrapper;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;


import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;

@RequiredArgsConstructor
public class authenticationfilter extends UsernamePasswordAuthenticationFilter{

	private final AuthenticationManager authenticationmanager;
	
	private final JwtService jwtservice;
	
	private final BCryptPasswordEncoder encode;
	
	private final MemberRepository repository;
	
	private final HistoryService historyservice;
	
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		// TODO Auto-generated method stub
		System.out.println("로그인시도 ");
		//HttpRequestWrapper requestwr=new HttpRequestWrapper((HttpServletRequest) request);
		HttpResRequestWrapper requestwr=new HttpResRequestWrapper(request);
		//직접만든필터
		ObjectMapper obmap=new ObjectMapper();
		
		
		try {
			//json파싱
			
			//인풋스트림으로 리퀘에서 바디로 가져올수있지만 ..한번쓰면못쓰게된다
			/*맵형식받기
			Map<String,String> userna=obmap.readValue(requestwr.getInputStream(),
					new TypeReference<Map<String,String>>(){});//맵형식을받는 타입레퍼런스
			
			System.out.println("맵형식:"+userna.get("username"));
			*/
			MemberEntity entity=obmap.readValue(requestwr.getInputStream(),MemberEntity.class);
			System.out.println("로그인정보:"+entity.getUsername());
			request.setAttribute("username", entity.getUsername());
			//어센티케이션토큰사용  자동으로 bcryp디코딩이되나봄? 
			UsernamePasswordAuthenticationToken authenticationtoken=
					new UsernamePasswordAuthenticationToken(entity.getUsername(),entity.getPassword());
			//매번서버가바뀌때 해쉬값이바뀌어서 저렇게 비교해야하는데 두번가야해서안효율적으로보이는디..더알아보자 토큰만들때 비번이흠;
			
			
			
			System.out.println("authentication실행여기서 유저디테일서비스들어가는듯 ");
			Authentication authentication=authenticationmanager.authenticate(authenticationtoken);
			
			System.out.println("이후프린시펄디테일자동실행ㅇ");
			
			 PrincipalDetails principalDetails=(PrincipalDetails) authentication.getPrincipal();
			   System.out.println("username: "+principalDetails.getUsername());
			   System.out.println("password: "+principalDetails.getPassword());
			   System.out.println("authorities: "+principalDetails.getMember().getNickname());
			   System.out.println("==================================");
			   //4.authentication반환
			   return authentication;
		
			   
		} catch (StreamReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		// TODO Auto-generated method stub
		System.out.println("5.인증완료:successfulAuthentication");
		//authresult에 저장되있음 
		//일단담아주는데나도모르겠다
		//SecurityContextHolder.getContext().setAuthentication(authResult);
		
		//
		PrincipalDetails principal=(PrincipalDetails) authResult.getPrincipal();
		System.out.println("프린시펄");
		System.out.println(principal);
		
		//임시유저객체생성 근데안해도될ㄷㅅ?
		/*
		MemberEntity mem=MemberEntity.builder()
				.id(principal.getMember().getId())
				.username(principal.getUsername())
				.name(principal.getName())
				.nickname(principal.getMember().getNickname())
				.build();
		*/
		String jwttoken=jwtservice.createtoken(principal);
		String refreshtoken=jwtservice.createrefreshtoken();
		
		
		
		//json형태로 쿠키에 여러값의 유저인포저장!
		JSONObject json= new JSONObject();
		
		json.put("username",principal.getUsername());
	
		json.put("nickname",principal.getMember().getNickname());
		
		json.put("region", principal.getMember().getHomeaddress().getJuso());
		json.put("gridx", principal.getMember().getHomeaddress().getGridx());
		json.put("gridy", principal.getMember().getHomeaddress().getGridy());
		json.put("profileimg", principal.getMember().getProfileimg());
		
		//쿠키에 = 등의기호와 한글은 저장안되기때문에 URLEncoder사용해서 저장
		Cookie idCookie=new Cookie("userinfo",URLEncoder.encode(json.toJSONString(),"UTF-8"));
		
		
		
		jwtservice.Setrefreshtoken(principal.getUsername(), refreshtoken);
		response.addCookie(idCookie);
	
		response.addHeader("Authorization", jwttoken);
		response.addHeader("Refreshtoken", refreshtoken);
		
		System.out.println("jwttoken:"+jwttoken);
		System.out.println("refreshtoken:"+refreshtoken);
		//로그인 히스토리 
		String clientip=historyservice.getrequestIp(request);
		LoginHistory history=LoginHistory.builder()
							.userid(principal.getUsername())
							.islogin(true)
							.clientip(clientip) //이부분좀봐야할듯
							.userdata(request.getLocale().toString())
							.build();
		historyservice.saveLoginlog(history);
				}
	 
	//로그인실패시!
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		// TODO Auto-generated method stub
		
			String code=failed.getMessage();
			String username=(String) request.getAttribute("username");
			System.out.println("로그인실패리스폰스"+code);
			
			if(code.equals("자격 증명에 실패하였습니다.")) {
			
			System.out.println("비밀번호실패");
				System.out.println("로그인실패리스폰스"+username);
				
				//이거근데자격증명코드가 비번틀렸을시만하자
				
				String clientip=historyservice.getrequestIp(request);
				LoginHistory history=LoginHistory.builder()
									.userid(username)
									.islogin(false)
									.clientip(clientip) //이부분좀봐야할듯
									.userdata(request.getLocale().toString())
									.build();
				historyservice.saveLoginlog(history);	
			}
			
						//에러핸들링스태틱유틸
			CustomResponseUtil.unAuthentication(response, code);
		 System.out.println("로그인실패!언섹");
		 
		//super.unsuccessfulAuthentication(request, response, failed);
	}
	
	
}
