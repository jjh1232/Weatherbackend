package com.example.firstproject.Handler;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.FavoriteEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.detachfile;

public interface NoticeHandler {
	
	Page<NoticeEntity> read(Pageable page);

	List<NoticeEntity> readfd(int page);
	
	void create(NoticeEntity entity);
	
	void delete(Long num);
	
	NoticeEntity update(NoticeEntity entity);
	
	Optional<NoticeEntity> findbyId(Long num);
	
	NoticeEntity detail(Long num);

	public void commentcreate(CommentEntity entity);
	
	List<CommentEntity> commentget(Long num);
	
	Page<NoticeEntity> searchtitle(String text,Pageable page);
	Page<NoticeEntity> searchtext(String text,Pageable page);
	Page<NoticeEntity> searchname(String text,Pageable page);
	
	Page<NoticeEntity> searchtitletext(String text,Pageable page);
	
	public Optional<CommentEntity> findcomment(Long id);
	
	public void deletecomment(Long id);
	public List<detachfile> getdatachfiles(String path);
	
	public Optional<FavoriteEntity>  findbynoticeanduser(MemberEntity member,NoticeEntity notice);

	public void favoritesave(FavoriteEntity favorite);
	public void favoritedelete(FavoriteEntity favorite);

	public Page<FavoriteEntity> favoritenoticefind(MemberEntity member,Pageable pageable);

}
