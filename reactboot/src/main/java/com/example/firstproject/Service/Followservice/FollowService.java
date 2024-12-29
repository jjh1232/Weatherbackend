package com.example.firstproject.Service.Followservice;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.firstproject.CustomError.ErrorCode;
import com.example.firstproject.Dto.follow.FollowDto;
import com.example.firstproject.Dto.follow.followlistDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.follow.FollowEntity;
import com.example.firstproject.Handler.FollowHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class FollowService {
	
	private final FollowHandler followhandler;
	
	public void follow(MemberEntity frommember, MemberEntity tomember) throws Exception {
		// TODO Auto-generated method stub
		if(frommember ==tomember) {
			//본인이 본인하는경우 
			throw new Exception("자기자신을팔로우할수없습니다");
			
		}
		//팔로우 이미한경우 보통 to와 프롬 값을 가져와서 비교
		if(followhandler.checkfollow(frommember.getId(), tomember.getId()).isPresent()) {
			//존재할경우트로 
			log.info("이미 팔로우한 계정입니다!");
			throw new Exception("이미한계쩡");
		}
		//위의 예외 모두지난후 팔로우 생성
		FollowEntity follow = FollowEntity.builder()
				.tomember(tomember)
				.frommember(frommember)
				.build();
		followhandler.save(follow);
		
		
		
		
	}
	
	//팔로워 찾기 나를팔로우한애들 팔로잉 한애들은 맴버객체에 연관관계로 불러옴
	public List<FollowDto> followerfind(Long userid){
		//투멤버찾아옴 
		List<FollowEntity> tolist=followhandler.followertofind(userid);
		if(tolist==null||tolist.isEmpty()) {
			log.info("팔로워없음!");
		}
		List<FollowEntity> mylist=followhandler.followerfromfind(userid);
		if(mylist ==null||mylist.isEmpty()) {
			log.info("내가팔로우한애없음");
		}
		
		List<FollowDto> followerlist=new ArrayList<>();
		
		for(FollowEntity toentity:tolist) {
			log.info("날구독한리스트시작"+toentity.getId());
			FollowDto dto=FollowDto.builder()
					.username(toentity.getFrommember().getUsername())
					.nickname(toentity.getFrommember().getNickname())
					.followcheck(false)
					.build();
			
		
			for(FollowEntity myentity:mylist) {
				log.info("내리스트시작");
				//내팔로우 목록의 투멤버와 날팔로우한목록에 프롬멤버가 같을경우
				if(myentity.getTomember().getUsername().equals(toentity.getFrommember().getUsername())) {
					log.info("나도팔로우한애");
					
					dto.setFollowcheck(true);
					log.info(dto.getNickname()+"님탈출하나?");
						break; //이거안쪽폴문 뚫을거임
				}
							
			}
		
		
			followerlist.add(dto);
		}
		//팔로우엔티티검사
		
		
		return followerlist;
	}
	//유저가 팔로우 했는지 안했는지여부찾기 유저클릭시 체크용 하나
	public boolean flowcheck(MemberEntity requestmember,MemberEntity selectmember) {
		if(followhandler.checkfollow(requestmember.getId(), selectmember.getId()).isPresent()) {
			//존재할경우트로 
			log.info("이미 팔로우한 계정입니다!");
			return true;
		}
		else {
				log.info("팔로우안되있는게쩡");
				return false;
			}
		}
	
	
		//삭제
	public void followdelete(MemberEntity frommember,MemberEntity deletemember) {
		log.info("삭제");
		FollowEntity deletefollow=followhandler.checkfollow(frommember.getId(), deletemember.getId()).orElseThrow();
		
		followhandler.deletefollow(deletefollow);
		
		
	}
	
	//팔로우관계찾기 그후 트루로변경
	public FollowEntity followfind(MemberEntity requestmember,MemberEntity tomember) {
		//팔로우엔티티가져옴
		FollowEntity entity=followhandler.checkfollow(requestmember.getId(), tomember.getId()).orElseThrow();
		
		entity.setFavorite(true);
		//변경감지가안됨 ..
		//followhandler.save(entity);
		return entity;
	}
	//팔로우관계찾기 그후 false로변경
		public FollowEntity unfollowfind(MemberEntity requestmember,MemberEntity tomember) {
			//팔로우엔티티가져옴
			FollowEntity entity=followhandler.checkfollow(requestmember.getId(), tomember.getId()).orElseThrow();
			
			entity.setFavorite(false);
			//변경감지가안됨 .. 트랜잭션안붙여서안된거엿음..
			//followhandler.save(entity);
			return entity;
		}
		
		public List<followlistDto> favoritefollow(MemberEntity member){
			List<FollowEntity> entitylist=followhandler.findfavoritefollow(member);
			List<followlistDto> dtolist=new ArrayList<>();
			if(entitylist.isEmpty()) {
				log.info("리스트가없습니다!");
				return null;
			}else {
			for(FollowEntity entity:entitylist) {
				followlistDto dto=followlistDto.builder()
						.username(entity.getTomember().getUsername())
						.nickname(entity.getTomember().getNickname())
						.favorite(true)
						.build();
				dtolist.add(dto);
			}
			
			return dtolist;
			}
			
		}
	
}
