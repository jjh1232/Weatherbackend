package com.example.firstproject.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Service.JwtService;
import com.example.firstproject.configure.PrincipalDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RefreshController {

	private final JwtService jwtservice;
	
	/* 리프레쉬 토큰 재발급.
	   예전엔 실패할 때 throw new Exception 을 던져서 500(서버 오류)으로 나갔다.
	   "토큰이 만료됐다"는 서버 잘못이 아니라 인증 문제라 401 이 맞고,
	   프론트도 401 을 보고 조용히 로그아웃 처리할 수 있다. */
	@GetMapping("/refresh")
	public ResponseEntity<Object> refreshtokenvalid(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.info("리프레쉬토큰실행");
		//여기서 엑세스토큰을 제발급하고 다시 authentication을하는느낌
		String refreshheader=request.getHeader("Refreshtoken");
		if(refreshheader !=null&&refreshheader.startsWith("Bearer ")) {
			log.info("리프레쉬토큰문제없음:"+refreshheader);
			String refreshtoken=refreshheader.replace("Bearer ", "");
			//리프레쉬토큰 유효기간체크
			if(jwtservice.checktokenvalid(refreshtoken)) {
				log.info("리프레쉬토큰기간유효!");
				//이거 리포지에저장해쓴ㄴ데 아니면 리프레쉬에 유저네임담고 그걸로 유저정보 가져온다
				MemberEntity member=jwtservice.findbyrefreshtoken(refreshtoken).orElseThrow();
				
				//프린시펄 등록
				PrincipalDetails principal=new PrincipalDetails(member);
				log.info("프린시펄등록완료");
				String newjwttoken=jwtservice.createtoken(principal);
				log.info("새엑세스토큰생성완료");
				//어센티케이션은 다시 액세스 사용하는걸로만들자 
				
				//리프레쉬토큰 시간계산후 리프레쉬토큰도 사용할지
				if(jwtservice.isneedrefreshtoken(refreshtoken)) {
					log.info("12시간이내에만료임 재발급");
					//새리프레쉬토큰생성후 유저데이터에 셋한다
					String newrefreshtoken=jwtservice.createrefreshtoken();
					System.out.println("재발급한리프레쉬토큰"+newrefreshtoken);
					jwtservice.Setrefreshtoken(principal.getUsername(), newrefreshtoken);
					response.addHeader("Authorization", newjwttoken);
					response.addHeader("Refreshtoken",newrefreshtoken);
				}
				else {
					log.info("리프레쉬토큰기간12시간이상!");
					//12시간보다많이남았을경우 액세스토큰만 다시 담아준다 
					response.addHeader("Authorization", newjwttoken);
					
				}
			}else {
				//리프레쉬토큰기간만료!
				log.info("리프레쉬토큰기간종료");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레쉬토큰기간종료");
			}
			
		}else {
			log.info("리프레쉬토큰이 없거나 잘못된 토큰");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레쉬토큰이없거나 잘못된토큰입니다");
		}
		
		//여기까지 왔으면 헤더에 새 토큰을 실어놨다
		return ResponseEntity.ok().build();
	}
}
