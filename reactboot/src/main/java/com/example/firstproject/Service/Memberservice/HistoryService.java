package com.example.firstproject.Service.Memberservice;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.LoginHistoryDto;
import com.example.firstproject.Entity.LoginHistory;
import com.example.firstproject.Repository.History.LoginhistoryRepository;

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
	
	public String getrequestIp(HttpServletRequest request) {
		 String ip = request.getHeader("X-Forwarded-For");
		    log.info("X-FORWARDED-FOR : " + ip);

		    if (ip == null) {
		        ip = request.getHeader("Proxy-Client-IP");
		        log.info("Proxy-Client-IP : " + ip);
		    }
		    if (ip == null) {
		        ip = request.getHeader("WL-Proxy-Client-IP");
		        log.info("WL-Proxy-Client-IP : " + ip);
		    }
		    if (ip == null) {
		        ip = request.getHeader("HTTP_CLIENT_IP");
		        log.info("HTTP_CLIENT_IP : " + ip);
		    }
		    if (ip == null) {
		        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		        log.info("HTTP_X_FORWARDED_FOR : " + ip);
		    }
		    if (ip == null) {
		        ip = request.getRemoteAddr();
		        log.info("getRemoteAddr : "+ip);
		    }
		    log.info("Result : IP Address : "+ip);

		    return ip;
	}
}
