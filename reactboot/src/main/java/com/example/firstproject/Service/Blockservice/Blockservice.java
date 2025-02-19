package com.example.firstproject.Service.Blockservice;

import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.block.NoticeblockEntity;
import com.example.firstproject.Handler.Blockhandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Blockservice {

	private final Blockhandler handler;
	
	public void noticeblock(MemberEntity member,Long noticeid) {
		
		NoticeblockEntity entity=NoticeblockEntity.builder().member(member).noticeid(noticeid).build();
		
		
		
		handler.noticeblock(entity);
		
	}
}
