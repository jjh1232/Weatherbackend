package com.example.firstproject.Handler;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.blockDto.Adminnoticedecleresponsedto;
import com.example.firstproject.Entity.block.NoticeblockEntity;
import com.example.firstproject.Entity.block.NoticedecleEntity;
import com.example.firstproject.Repository.NoticeDeclerepository;
import com.example.firstproject.Repository.NoticeblockRepository;

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
}
