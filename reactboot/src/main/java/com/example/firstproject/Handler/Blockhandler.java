package com.example.firstproject.Handler;

import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.block.NoticeblockEntity;
import com.example.firstproject.Repository.NoticeblockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class Blockhandler {

	private final NoticeblockRepository repository;
	
	public void noticeblock(NoticeblockEntity entity) {
		
		repository.save(entity);
		
		
	}
}
