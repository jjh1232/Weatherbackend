package com.example.firstproject.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	
	@GetMapping("/refresh")
	public void refreshtokenvalid(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
				throw new Exception("리프레쉬토큰기간종료!");
			}
			
		}else {
			throw new Exception("리프레쉬토큰이없거나 잘못된토큰입니다!");
		}
		
		//리턴값을 어케줘야하지 ?
		
	}
}
