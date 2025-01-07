package com.example.firstproject.configure.auth;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Optional;

import javax.lang.model.type.ErrorType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.firstproject.CustomError.CustomException;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Service.JwtService;
import com.example.firstproject.configure.PrincipalDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.protocol.Security;

import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;

//@RequiredArgsConstructor //베이직어센티케이션필터는기본생성자가없나봄

public class Authorizationdfilter extends BasicAuthenticationFilter{
	
	

	private final MemberRepository repository;
	
	private final JwtService jwtservice;
	
	public Authorizationdfilter(AuthenticationManager authenticationManager,MemberRepository repository,JwtService jwtservice) {
		super(authenticationManager);
		this.repository=repository;
		this.jwtservice=jwtservice;
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		//String path = request.getServletPath();
		//System.out.println(path);
		
		//if(path.contains("/athen")) {
		
		System.out.println("인가서비스");
		System.out.println("인가서비스요청주소:"+request.getServletPath());
		
		if(request.getServletPath().equals("/refresh")) {
			System.out.println("리프레쉬토큰일경우 넘긴다 ");
			chain.doFilter(request, response);
		}
		else {
		String jwtheader=request.getHeader("Authorization");
		//String refreshheader=request.getHeader("Refreshtoken");
		
		System.out.println("jwtheader: "+jwtheader);
		System.out.println("===========================================");
		
		
		if(jwtheader==null || !jwtheader.startsWith("Bearer ")) {
			System.out.println("잘못된토큰이거나토큰이없습니다");
			//여기 jwt에러 
		
			
		}
		else {
		System.out.println("토큰이제대로됫음");
		String jwttoken=jwtheader.replace("Bearer ","");
		System.out.println("jwttoken"+jwttoken);
		
		//토큰 기간체크 
		if(jwtservice.checktokenvalid(jwttoken)) {
			System.out.println("엑세스토큰사용가능");
			String username=jwtservice.gettokenclaim(jwttoken);
			
			Optional<MemberEntity> opentity=repository.findByUsername(username);
			
			MemberEntity entity=opentity.get();
			PrincipalDetails principal=new PrincipalDetails(entity);
			Authentication authentication=new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities() );
			System.out.println("로그인유저권한넣기전:"+principal.getAuthorities());
			if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("User"))) {
				System.out.println("어드민유저인가");
			}
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			String Accesstoken=jwtservice.createtoken(principal);
		
			String Refreshtoken=jwtservice.createrefreshtoken();
			//두필터에서는 헤더수정이안됨;;
			jwtservice.Setrefreshtoken(entity.getUsername(), Refreshtoken);
			response.addHeader("Authorization",Accesstoken);
			response.addHeader("Refreshtoken",Refreshtoken);
			
			
			
			System.out.println(Accesstoken);
			System.out.println("유저인증완료!");
			chain.doFilter(request, response);//이게정상으로넘어감
			return;//두필터로안넘어간당..
		}else {
			tokenExceptionhandler(response,"엑세스토큰만료");
			return;//리턴으로멈춰야함
		}
		
		}
		}// 처음엘스
		}
		/*
		
		//엑세스타큰사용불가시리프레쉬토큰조회 
		if(refreshheader==null ||!refreshheader.startsWith("Bearer ")) {
			System.out.println("리프레쉬토큰이잘못되었습니다 다시로그인해주세요");
			//chain.doFilter(request, response);
			
			return;
			
		}
		String refreshtoken=refreshheader.replace("Bearer ", "");
		if(jwtservice.isneedrefreshtoken(refreshtoken)) {
			System.out.println("리프레쉬토큰 사용가능");
			//리프레쉬토큰시간계산
			//액세스토큰재발급?
			System.out.println("dma"+refreshtoken);
			MemberEntity entity=repository.findByrefreshtoken(refreshtoken).orElseThrow();
			
			System.out.println("리프레쉬토큰으로겟");//영속성컨테스트문제라는데왜안되지이거엔티티불러오면.. ;
			
			
			PrincipalDetails principal=new PrincipalDetails(entity);
			System.out.println("다시액세스토큰정보"+principal);
			String newjwttoken=jwtservice.createtoken(principal);
			//리프레쉬토큰재발급
			String newrefreshtoken=jwtservice.createrefreshtoken();
			jwtservice.Setrefreshtoken(principal.getUsername(), refreshtoken);
			//재발급 
			System.out.println("토큰두개재발급");
			response.addHeader("Authorization", newjwttoken);
			response.addHeader("Refreshtoken",newrefreshtoken);
			
	JSONObject json= new JSONObject();
			
			json.put("username",principal.getUsername());
		
			json.put("nickname",principal.getMember().getNickname());
			
			
			Cookie idCookie=new Cookie("userinfo",URLEncoder.encode(json.toJSONString(),"UTF-8"));
			
			idCookie.setPath("/");//사용가능한패스
	     
	     response.addCookie(idCookie);
			//어센티케이션객체생성
			Authentication authentication= //아이디,비번,권한
					new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
			//시큐리티세션에 authentication저장 
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			System.out.println("refreshtoken으로인증완료");
			chain.doFilter(request, response);
			
			return;
		}
		//리프레쉬토큰사용불가시 재로그인필요 
		
		
		//엑세스토큰부터검증 
		System.out.println("리프레쉬토큰도만료되었습니다 다시로그인해주세요");
		System.out.println("이거무조건인가?");
		
		tokenExceptionhandler(response,"리프레쉬만료일듯");
		
	//에러처리
		
		
	//	throw new IOException("refresherror"); 필터내의예외처리를 해야함 디스페처서블릿에서 여기예외처리는처리안함
		//chain.doFilter(request, response); 다음필터로
		}
	}
	/*
		else {
			System.out.println("인가가필요하지않음");
			chain.doFilter(request, response);
		}
		}
		*/
		
	
	//필터내의 예외처리
	public void tokenExceptionhandler(HttpServletResponse response,String message) {
	System.out.println("토큰만료 메세지:"+message);
	response.setStatus(401);
	response.setContentType("application/json");
	response.setCharacterEncoding("UTF-8");
	
	/*
	response.setStatus(error.getCode());
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    try {
        String json = new ObjectMapper().writeValueAsString(MessageResponseDto.of(error.getCode(), error.getMessage()));
        response.getWriter().write(json);
    } catch (Exception e) {
        log.error(e.getMessage());
    }
    */
	}	
}


