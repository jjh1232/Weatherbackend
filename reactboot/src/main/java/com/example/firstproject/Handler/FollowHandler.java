package com.example.firstproject.Handler;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.follow.FollowEntity;
import com.example.firstproject.Repository.FollowRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowHandler {

	
	private final FollowRepository followrepository;
	
	public Optional<FollowEntity> checkfollow(Long followid,Long followerid) {
		log.info("팔로우핸들러!");
		return followrepository.checkfollow(followid,followerid);
		
		
	}
	
	public void save(FollowEntity entity) {
		log.info("세이브핸들러");
	
		followrepository.save(entity);
	
	}
	
	public List<FollowEntity> followertofind(Long userid){
		log.info("팔로워찾기");
		return followrepository.findBytoMember(userid);
	}
	//프롬멤버
	public List<FollowEntity> followerfromfind(Long userid){
		log.info("팔로워찾기");
		return followrepository.findByFromMember(userid);
	}
	
	public void deletefollow(FollowEntity entity) {
		log.info("딜리트핸들러");
		followrepository.delete(entity);
	}
	
	public List<FollowEntity> findfavoritefollow(MemberEntity member){
		
		
		return followrepository.findBytoMemberandfavorite(member.getId());
	}
	
}
