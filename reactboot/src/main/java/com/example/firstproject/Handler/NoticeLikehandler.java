package com.example.firstproject.Handler;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.FavoriteEntity;
import com.example.firstproject.Repository.LikeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeLikehandler {

	private final LikeRepository likerepo;
	
	public Long likecheck(Long noticeid) {
		
		return null;
		
	}
	public 	Optional<FavoriteEntity> favoritecheck(Long userid,Long noticeid) {
		
		Optional<FavoriteEntity> entity=likerepo.findByNoticeNoticeidAndMemberId(noticeid, userid);
		return entity;
	}
	
	public void deleteFavoritenotice(FavoriteEntity entity) {
		likerepo.delete(entity);
	}
	
}
