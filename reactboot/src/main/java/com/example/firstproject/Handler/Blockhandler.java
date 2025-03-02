package com.example.firstproject.Handler;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.blockDto.Adminnoticedecleresponsedto;
import com.example.firstproject.Entity.block.NoticeblockEntity;
import com.example.firstproject.Entity.block.NoticedecleEntity;
import com.example.firstproject.Repository.NoticeDeclerepository;
import com.example.firstproject.Repository.NoticeblockRepository;
import com.mysql.cj.log.Log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class Blockhandler {

	private final NoticeblockRepository blockrepository;
	
	private final NoticeDeclerepository declerepository;
	
	public void noticeblock(NoticeblockEntity entity) {
		
		blockrepository.save(entity);
		
		
	}
	public void noticedecleadd(NoticedecleEntity entity) {
		declerepository.save(entity);
	}


	//귀찮으니까 페이지로받자
	public Page<NoticedecleEntity> findbynoticeidget(Pageable pageable,Long noticeid){
		Page<NoticedecleEntity> entity=declerepository.findbyNoticeid(noticeid, pageable);
		
		return entity;
	}
	
	public boolean noticeblockcheck(Long userid,Long noticeid) {
		log.info("블록핸들러실행"+userid + "또"+noticeid);
		//이거 boolean값을 못받네;; 변환을못시킴 query로카운트로만해야하는데 성능이안좋음
		//그냥여기서 if문으로해야할듯 querydsl쓰면되긴한다는데흠
		Long check=blockrepository.noticeblockcheck(userid, noticeid);
		log.info("블록핸들러종료"+check);
		if (check==1) {
			return true;
		}else {
		return false;
		}
		}
	
	public boolean noticedeclecheck(Long userid,Long noticeid) {
		log.info("디클핸들러실행"+userid + "또"+noticeid);
		Long check=declerepository.noticeblockcheck(userid, noticeid);
		log.info("디클핸들러종료"+check);
		if (check==1) {
			return true;
		}else {
		return false;
		}
	}
	//유저블록리스트
	public List<NoticeblockEntity> getuserblock(Long userid) {
		
		List<NoticeblockEntity> blocks=blockrepository.userblocklist(userid);
		
		return blocks;
		
	}
	//유저블록테스트 2
	
	public List<Long> getblocknoticenum(Long userid){
		
		List<Long> blocks=blockrepository.userblocknoticeid(userid);
		
		return blocks;
	}
	
}
