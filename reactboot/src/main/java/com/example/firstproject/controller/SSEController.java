package com.example.firstproject.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.firstproject.Dto.userdataDto.NotifiResult;
import com.example.firstproject.Dto.userdataDto.NotificationDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Repository.EmitterRepository;
import com.example.firstproject.Service.JwtService;
import com.example.firstproject.Service.Memberservice.MemberService;
import com.example.firstproject.Service.Memberservice.SseService;
import com.example.firstproject.configure.PrincipalDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
//@CrossOrigin("*")
public class SSEController {

		//연결 보관은 EmitterRepository 가 전부 맡는다.
	//예전에 여기 있던 public static Map<Long,SseEmitter> 는 아무도 쓰지 않는 죽은 필드였다.
	private final MemberService memberservice;
	
	private final EmitterRepository emitterrepo;
	
	private final SseService sseservice;

	private final JwtService jwtservice;
	//======================SSE알림 서비스=======================
	
		@GetMapping(value="/ssesub", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter sse(Authentication authentication, HttpServletResponse response) {

		//nginx·클라우드플레어 같은 앞단이 응답을 모아뒀다 보내면 실시간이 아니게 된다.
		response.setHeader("X-Accel-Buffering", "no");
		response.setHeader("Cache-Control", "no-cache");

		//
		PrincipalDetails cipal=(PrincipalDetails) authentication.getPrincipal();
		
		Long userid=cipal.getMember().getId();
		System.out.println("어센티케이션유저아이디:"+userid);
		
		//리턴을안해주면 전달이안된다
		return sseservice.SSEcon(userid);
	}
	//노티피케이션도 에미터에서하자
	@GetMapping("/notification")
	public ResponseEntity usernotifi(Authentication authentication,@RequestParam(name = "page", defaultValue = "1") int page){
PrincipalDetails cipal=(PrincipalDetails) authentication.getPrincipal();
		
		MemberEntity member=cipal.getMember();
		System.out.println("어디가문제임");
		NotifiResult<NotificationDto> notilist=sseservice.getusernotifi(member.getId(), page);
		System.out.println("어디가문제임1");
		
		return ResponseEntity.ok(notilist);
		
		
	}
	@GetMapping("/emittercheck")
	public void emittercheck() {
		
		emitterrepo.getemitteruser();
		emitterrepo.getAllEmiter();
	}
	@GetMapping("/notificationcount")
	public ResponseEntity notificount(Authentication authentication) {
PrincipalDetails cipal=(PrincipalDetails) authentication.getPrincipal();
		
		MemberEntity member=cipal.getMember();
		
		Long count=sseservice.notificationcount(member.getId());
		
		return ResponseEntity.ok(count);
	}
	
	@PostMapping("/notification/readall")
	public void notifireadall(Authentication authenti) {
PrincipalDetails cipal=(PrincipalDetails) authenti.getPrincipal();
		
		MemberEntity member=cipal.getMember();
		System.out.println("리드올 on");
		sseservice.readallnotify(member.getId());
		
	}

	
	//로그아웃시 백엔드처리가 사실 sse뿐이라여기나둠
	@GetMapping("/memberlogout")
	public ResponseEntity<?> memberlogout(Authentication authentication) {
		System.out.println("멤버로그아웃시작");
		//액세스 토큰이 이미 만료된 채로 로그아웃을 누를 수도 있다.
		//그 경우 인증이 없으니 서버가 지울 것도 없고, 프론트는 쿠키만 지우면 된다.
		if(authentication==null || !(authentication.getPrincipal() instanceof PrincipalDetails)) {
			return ResponseEntity.ok("로그아웃");
		}
		PrincipalDetails prin=(PrincipalDetails) authentication.getPrincipal();
		Long userid=prin.getMember().getId();
		
		sseservice.deleteemiter(userid);

		//저장된 리프레쉬 토큰을 지운다. 안 지우면 로그아웃한 뒤에도 그 토큰으로
		///refresh 가 통해서 세션이 되살아난다.
		jwtservice.clearrefreshtoken(prin.getUsername());
		
		
		return ResponseEntity.ok(userid+"에미터삭제");
		
		
	}
	
}
