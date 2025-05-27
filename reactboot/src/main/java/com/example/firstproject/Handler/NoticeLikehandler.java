package com.example.firstproject.Handler;

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
	public FavoriteEntity favoritecheck() {
		
		return null;
	}
	
	
}
