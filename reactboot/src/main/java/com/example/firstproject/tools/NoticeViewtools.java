package com.example.firstproject.tools;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.firstproject.Repository.NoticeRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NoticeViewtools {

	private final RedisTemplate<String, String> redisTemplate;
	
	private final NoticeRepository noticerepo;
	
	public void increaseviewcount(Long noticeid) {
		String key="viewcount:"+noticeid;
		//key값다음증가량도설정가능 지정안할시 1
		redisTemplate.opsForValue().increment(key);//위의키값으로 접근후 증가 (스트링으로저장되지만뭐알아서바꿔준다함)
		//유효기간설정
		//근데어차피 지우기때문에 그냥 안정성을위해 나둠
		redisTemplate.expire(key, 1,TimeUnit.DAYS);
		
	}
	
	@Scheduled(fixedDelay = 60000)//1분마다
	@Transactional
	public void ViewCountTODB() {
		//뷰카운트가져오기 저걸로시작하는거 다가져옴
		Set<String> keys=redisTemplate.keys("viewcount:*");
		for(String key : keys) {
			Long noticeid=extractnoticeid(key);
			
			Long viewcount =Long.valueOf(redisTemplate.opsForValue().get(key));
			//저장로직
			noticerepo.updateviewcount(noticeid,viewcount);
			//동기화후 삭제
			redisTemplate.delete(key);
		}
	}
	//노티스아이디구하기
	private Long extractnoticeid(String key) {
		String[] parts=key.split(":");
		//혹시나 잘못된배열이 들어올경우를 대비해서 [count:id] 이런식으로2개가나와야함 :이기준으로나눠서
		if(parts.length==2) {
			try {
				return Long.parseLong(parts[1]);
			}catch(NumberFormatException e) {
				return null;
			}
		}
	return null;
	}
}
