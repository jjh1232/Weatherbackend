package com.example.firstproject.configure.auth;

import java.io.IOException;
import java.util.Optional;

import javax.lang.model.type.ErrorType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
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
		//=====================================================================
		// 업로드 이미지(WebConfig.addResourceHandlers 가 내보내는 정적 파일)는 그냥 통과시킨다.
		//
		// <img src="..."> 요청에는 브라우저가 Authorization 헤더를 절대 붙이지 않는다.
		// 그런데 이 경로들은 "/open/" 이 아니라서 아래 분기로 내려가는데,
		// 토큰이 없으면 chain.doFilter() 도 에러 응답도 없이 그냥 끝나버린다.
		// 결과는 200 + Content-Length: 0 - 이미지가 통째로 안 보인다.
		//
		// 예전엔 이 파일들을 리액트 개발서버가 public 폴더에서 직접 내보내서
		// 백엔드를 아예 거치지 않았다. 배포하면 프론트가 정적 호스팅이라
		// 서버가 내보내야 하고(WebConfig 주석 참고), 그때부터 이 필터를 타게 됐다.
		//
		// 프로필 사진과 게시글 이미지는 글목록에서 누구에게나 보여야 하는 공개 자원이다.
		//=====================================================================
		else if(isuploadedimage(request.getServletPath())) {
			chain.doFilter(request, response);
		}
		else {
	
		String jwtheader=request.getHeader("Authorization");
		//String refreshheader=request.getHeader("Refreshtoken");
		
		
		System.out.println("jwtheader: "+jwtheader);
		System.out.println("===========================================");
	if(request.getServletPath().startsWith("/open/")) {
			//open경로===============================================================================
		
		if (jwtheader != null && jwtheader.startsWith("Bearer ")) {
		        // 로그인 유저: 토큰 검증
			System.out.println("오픈된경로이지만 유저헤더가있음");
		        String jwttoken = jwtheader.replace("Bearer ", "");
		        if (jwtservice.checktokenvalid(jwttoken)) {
		            // 인증 정보 세팅
		            String username = jwtservice.gettokenclaim(jwttoken);
		            Optional<MemberEntity> opentity = repository.findByUsername(username);
		            if (opentity.isPresent()) {
		                MemberEntity entity = opentity.get();
		                PrincipalDetails principal = new PrincipalDetails(entity);
		                Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		                SecurityContextHolder.getContext().setAuthentication(authentication);

		                // 토큰 재발급은 만료가 가까울 때만 (아래 인증 경로와 같은 정책)
		                if (jwtservice.isaccessrenewneeded(jwttoken, JwtService.ACCESS_RENEW_MINUTES)) {
		                	response.addHeader("Authorization", jwtservice.createtoken(principal));
		                }
		            }
		        }
		        // 토큰이 잘못됐으면 그냥 비회원으로 통과시킬지, 에러 응답을 줄지는 정책에 따라 결정
		        // 여기서는 에러 응답을 주는 게 일반적
		        else {
		            tokenExceptionhandler(response, "엑세스토큰만료");
		            return;
		        }
		    }
		    // 비로그인 유저: 그냥 통과
	//	System.out.println("open이고비로그인유저");
		    chain.doFilter(request, response);
		    return;
		}
		
	
	//open아닌경로
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
			
			//=====================================================================
			// 토큰 재발급 - 만료가 가까울 때만 한다.
			// 예전엔 요청마다 액세스/리프레쉬를 새로 발급하고 DB 에도 덮어썼다. 그래서
			//  1) 프론트가 요청을 병렬로 날리면(글목록·날씨·채팅방메타·알림수가 같이 나간다)
			//     DB 에는 마지막에 커밋된 리프레쉬 토큰이, 쿠키에는 마지막에 도착한 응답의
			//     리프레쉬 토큰이 남는다. 둘이 어긋난 뒤 401 이 한 번 뜨면 /refresh 의
			//     findbyrefreshtoken(...).orElseThrow() 가 터져서 강제 로그아웃이 됐다.
			//  2) 요청마다 DB UPDATE 가 한 번씩 나갔다.
			//  3) 응답 헤더가 매번 달라지니 프론트가 쿠키를 다시 쓰고, 그 쿠키 변경 때문에
			//     화면 전체가 다시 그려지고 SSE 도 끊었다 붙었다 했다.
			// 리프레쉬 토큰 회전은 /refresh(RefreshController)가 12시간 규칙으로 맡는다.
			// 쓰는 동안 세션이 연장되는 동작은 그대로다(만료 10분 전에 갱신된다).
			//=====================================================================
			if(jwtservice.isaccessrenewneeded(jwttoken, JwtService.ACCESS_RENEW_MINUTES)) {
				System.out.println("액세스토큰 만료가 가까워 재발급");
				response.addHeader("Authorization",jwtservice.createtoken(principal));
			}

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
			
			//화면 표시용 유저 정보도 같이 새로 내려준다.
			//예전엔 username/nickname 만 넣어서, 재발급이 한 번 일어나면
			//userid·region·gridx·gridy·profileimg·userrole 이 통째로 사라졌다.
			//쿠키가 아니라 응답 헤더로 보낸다(tools/Userinfoheader 참고).
			//Userinfoheader.write(response, entity);
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
		
	
	/** WebConfig.addResourceHandlers() 가 등록한 업로드 이미지 경로인가. */
	private boolean isuploadedimage(String path) {
		return path.startsWith("/noticeimages/")
				|| path.startsWith("/userprofileimg/")
				|| path.startsWith("/userbackgroundimg/");
	}

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


