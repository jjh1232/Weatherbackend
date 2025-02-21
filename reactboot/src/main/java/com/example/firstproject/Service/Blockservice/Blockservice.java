package com.example.firstproject.Service.Blockservice;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.blockDto.NoticeblockDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.block.NoticeblockEntity;
import com.example.firstproject.Entity.block.BlockEnum.NoticeblockEnum;
import com.example.firstproject.Handler.Blockhandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class Blockservice {

	private final Blockhandler handler;
	
	public void noticeblock(MemberEntity member,NoticeblockDto dto) {
		
		log.info("노티스아이디:"+dto.getNoticeid());
		log.info("리즌:"+dto.getReason());
		Set<NoticeblockEnum> enu=new HashSet<>();
		enu.add(NoticeblockEnum.baduser);
		
		NoticeblockEntity entity=NoticeblockEntity.builder().member(member)
				.noticeid(dto.getNoticeid())			
				.reason(NoticeblockEnum.baduser)
				.build();
		
		
		log.info("엔티티리즌:"+entity.getReason());
		/*
		for(NoticeblockEnum data:dto.getReason()) {
			log.info("리스트순회로맵세팅:"+data);
			switch (data) {
			case spam: {
				entity.addreason(NoticeblockEnum.spam);
				break;
			}	case discomfort: {
				entity.addreason(NoticeblockEnum.discomfort);
				break;
			}	case violent: {
				entity.addreason(NoticeblockEnum.violent);
				break;
			}	case nsfw: {
				entity.addreason(NoticeblockEnum.nsfw);
				break;
			}	case nointerested: {
				entity.addreason(NoticeblockEnum.nointerested);
				break;
			}	case baduser: {
				entity.addreason(NoticeblockEnum.baduser);
				break;
			}	case noreason: {
				entity.addreason(NoticeblockEnum.noreason);
				break;
			}	case etc: {
				entity.addreason(NoticeblockEnum.etc);
				break;
			}
			default:
				throw new IllegalArgumentException("일치하는값이없네용") ;
			}
			
		}
		
		*/
		
		handler.noticeblock(entity);
		
	}
}
