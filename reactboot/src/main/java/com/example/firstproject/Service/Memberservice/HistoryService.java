package com.example.firstproject.Service.Memberservice;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.LoginHistoryDto;
import com.example.firstproject.Entity.LoginHistory;
import com.example.firstproject.Repository.History.LoginhistoryRepository;
import com.example.firstproject.tools.ClientIp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService {

	private final LoginhistoryRepository loginhistoryrepo;
	
	
	public void saveLoginlog(LoginHistory history) {
		loginhistoryrepo.save(history);
		log.info("로그인히스토리저장성공");
	}
	
	public List<LoginHistoryDto> getUserloginhistory(String userid){
		List<LoginHistory> historyEntity=loginhistoryrepo.findByuseridOrderByLogindtDesc(userid);
		
		return historyEntity.stream()  //스트림으로변환
				.map(LoginHistoryDto::fromEntity) //원하는형식으로변환 클래스이름::메소드이름실행 으로변환
				.collect(Collectors.toList()); //스트림요소를 원하는자료형으로변환 
				//toList()리스트로 반환 aslist 는 예시리스트라고함
	}
	
	/**
	 * 로그인 이력에 남길 클라이언트 IP.
	 *
	 * <p>예전엔 X-Forwarded-For 만 봤다. 클라우드플레어 뒤에 두면 그 헤더가 없거나
	 * 프록시 체인이 들어와서 <b>모든 로그인이 같은 IP 로 기록</b>됐다.
	 * 이력 자체가 "낯선 곳에서 로그인됨"을 보라고 있는 기능이라 그러면 의미가 없다.
	 * 판별 로직은 요청 제한(RateLimitInterceptor)과 같은 것을 쓴다.
	 */
	public String getrequestIp(HttpServletRequest request) {
		String ip = ClientIp.resolve(request);
		log.info("Result : IP Address : " + ip);
		return ip;
	}
}
