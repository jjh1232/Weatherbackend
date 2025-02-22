package com.example.firstproject.Service.Blockservice;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.blockDto.NoticeblockDto;
import com.example.firstproject.Dto.blockDto.NoticedecleDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.block.NoticeblockEntity;
import com.example.firstproject.Entity.block.NoticedecleEntity;
import com.example.firstproject.Entity.block.BlockEnum.NoticeblockEnum;
import com.example.firstproject.Handler.Blockhandler;
import com.example.firstproject.Handler.NoticeHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class Blockservice {

	private final Blockhandler handler;
	
	private final NoticeHandler noticehandler;
	
	public void noticeblock(MemberEntity member,NoticeblockDto dto) {
		
		log.info("노티스아이디:"+dto.getNoticeid());
		log.info("리즌:"+dto.getReason());
		
		//빈깡통추가
		EnumSet<NoticeblockEnum> blockreason=EnumSet.noneOf(NoticeblockEnum.class);
		
		//이거말곤추가할방법없나 ..case늘수록 오히려더느릴텐디
		for(NoticeblockEnum data:dto.getReason()) {
			log.info("리스트순회로맵세팅:"+data);
			switch (data) {
			case spam: {
				blockreason.add(NoticeblockEnum.spam);
				break;
			}	case discomfort: {
				blockreason.add(NoticeblockEnum.discomfort);
				break;
			}	case violent: {
				blockreason.add(NoticeblockEnum.violent);
				break;
			}	case nsfw: {
				blockreason.add(NoticeblockEnum.nsfw);
				break;
			}	case nointerested: {
				blockreason.add(NoticeblockEnum.nointerested);
				break;
			}	case baduser: {
				blockreason.add(NoticeblockEnum.baduser);
				break;
			}	case noreason: {
				blockreason.add(NoticeblockEnum.noreason);
				break;
			}	case etc: {
				blockreason.add(NoticeblockEnum.etc);
				break;
			}
			default:
				throw new IllegalArgumentException("일치하는값이없네용") ;
			}
			
		}
		NoticeblockEntity entity=NoticeblockEntity.builder().member(member)
				.noticeid(dto.getNoticeid())			
				.reason(blockreason)
				.build();
		
		
		log.info("엔티티리즌:"+entity.getReason());
		
		
		
		
		
		handler.noticeblock(entity);
		
	}
	
	public void Noticedecle(MemberEntity member,NoticedecleDto dto) {
		
		//enum안쓰고 걍스트링으로하기 ,표를기준으로할려함
		
		NoticeEntity noticeentity=noticehandler.findbyId(dto.getNoticeid()).orElseThrow(()->new IllegalAccessError("게시글없음;"));
		
		NoticedecleEntity entity=NoticedecleEntity.builder()
				.member(member)
				.notice(noticeentity)
				.reason(dto.getReason())
				.build();
		
		handler.noticedecleadd(entity);
		
		
	}
}
