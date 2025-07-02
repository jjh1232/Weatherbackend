package com.example.firstproject.Service.Blockservice;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.blockDto.NoticeblockDto;
import com.example.firstproject.Dto.blockDto.NoticedecleDto;
import com.example.firstproject.Entity.FavoriteEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.block.NoticeblockEntity;
import com.example.firstproject.Entity.block.NoticedecleEntity;
import com.example.firstproject.Entity.block.BlockEnum.NoticeblockEnum;
import com.example.firstproject.Handler.Blockhandler;
import com.example.firstproject.Handler.NoticeHandler;
import com.example.firstproject.Handler.NoticeLikehandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.firstproject.Dto.blockDto.Adminnoticedecleresponsedto;
@Service
@RequiredArgsConstructor
@Slf4j
public class Blockservice {

	private final Blockhandler handler;
	
	private final NoticeHandler noticehandler;
	
	private final NoticeLikehandler likehandler;
	
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
		//좋아여여부 체크
		Optional<FavoriteEntity> likecheckentity=likehandler.favoritecheck(member.getId(), dto.getNoticeid());
		/* 구시대래
		if(likecheckentity.isPresent()) {
			likehandler.deleteFavoritenotice(likecheckentity.get());
		}
		*/
		//신시대
		likecheckentity.ifPresent(favoriteentity->{
			likehandler.deleteFavoritenotice(favoriteentity);
		});
		//더신시대
		//likehandler.favoritecheck(member.getId(), dto.getNoticeid()).ifPresent(likehandler::deleteFavoritenotice);
		
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
				.reason(dto.getReason().toString())
				.build();
		
		handler.noticedecleadd(entity);
		
		
	}
	//노티스에서 신고정보가져오기 운영자만
	public Page<Adminnoticedecleresponsedto> noticedecledata(Long noticeid,int page){
		PageRequest pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate"));
		//일단 아이디로 해보자 안되면 노티스엔티티가져와야;
		
		Page<NoticedecleEntity> entitylist=handler.findbynoticeidget(pageable, noticeid);
		
		Page<Adminnoticedecleresponsedto> dtolist=entitylist.map((m)->{
			
			return Adminnoticedecleresponsedto.builder().username(m.getMember().getUsername())
					.noticeid(m.getNotice().getNoticeid())
					.datetime(m.getCreatedDate())
					.reason(m.getReason())
					.build();
					
					
					
		});
		
		return dtolist;
		
	}

	public boolean noticeblockcheck(Long userid,Long noticeid) {
		boolean check=handler.noticeblockcheck(userid,noticeid);
		
		return check;
	}

	public boolean noticedeclecheck(Long userid,Long noticeid) {
	boolean check=handler.noticedeclecheck(userid,noticeid);
		
		return check;
	}
	
	
	//게시글 신고 차단 취소로직 서비스======================================================
	
	public void blockcancle(Long memberid,Long noticeid) throws IllegalAccessException {
		NoticeblockEntity entity=handler.blockfindbyuseridandnoticeid(memberid, noticeid).orElseThrow(()->new IllegalAccessException("차단목록없음"));
		handler.deletenoticeblock(entity);
		
		
	
	}
	
	public void declecancel(Long memberid,Long noticeid) throws IllegalAccessException {
		NoticedecleEntity entity=handler.declefindbyuseridandnoticeid(memberid, noticeid).orElseThrow(()->new IllegalAccessException("신고목록없음"));
		handler.deletenoticedecle(entity);
	}
	
	//정보까지 가져오기
	public Optional<NoticeblockEntity> getblock(Long memberid,Long noticeid) throws IllegalAccessException {
		Optional<NoticeblockEntity> entity=handler.blockfindbyuseridandnoticeid(memberid, noticeid);
	
		return entity;
	}
	public Optional<NoticedecleEntity> getdecle(Long memberid,Long noticeid) throws IllegalAccessException {
		Optional<NoticedecleEntity> entity=handler.declefindbyuseridandnoticeid(memberid, noticeid);
	
		return entity;
	}
}
