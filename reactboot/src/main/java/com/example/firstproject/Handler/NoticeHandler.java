package com.example.firstproject.Handler;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.firstproject.Dto.NoticeDetailDto;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.NoticeDtointer;
import com.example.firstproject.Dto.NoticeImageDto;
import com.example.firstproject.Dto.TwitformnoticeDto;
import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.FavoriteEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.detachfile;

public interface NoticeHandler {
	
	Page<TwitformnoticeDto> twitformnoticelist(Long userid,Pageable page);
	
	Page<TwitformnoticeDto> searchtwitform(Long userid,String option,String keyword,Pageable page);

	
	List<NoticeEntity> readfd(int page);
	
	void create(NoticeEntity entity);
	
	void delete(Long num);
	
	NoticeEntity update(NoticeEntity entity);
	
	Optional<NoticeEntity> findbyId(Long num);
	
	NoticeDetailDto detail(Long num);

	public void commentcreate(CommentEntity entity);
	
	List<CommentEntity> commentget(Long num);
	

	
	public Optional<CommentEntity> findcomment(Long id);
	
	public void deletecomment(Long id);
	public List<detachfile> getdatachfiles(String path);
	
	public Optional<FavoriteEntity>  findbynoticeanduser(MemberEntity member,NoticeEntity notice);

	public void favoritesave(FavoriteEntity favorite);
	public void favoritedelete(FavoriteEntity favorite);

	public Page<FavoriteEntity> favoritenoticefind(MemberEntity member,Pageable pageable);
	
	public List<Long> favoritenoticeids(Long userid,List<Long> noticeids);

	public Page<NoticeEntity> findbyidall(Long userid,Pageable pageable);
	
	public Page<CommentEntity> showcomments(Long userid,Pageable pageable);
	
	public Page<CommentDto> showdirectc(Long noticeid,Pageable pageable);
	
	//좋아요한게시글 가져오기
	public Page<NoticeEntity> getfavoritelist(MemberEntity member,Pageable pageable);

	//좋아요게시글서치
	public Page<NoticeEntity> favoritenoticesearch(MemberEntity member,Pageable pageable,String option,String keyword);

	//좋아요여부 id로
	public boolean Likenoticecheck(Long userid,Long noticeid);
	
	//이미지게시판
	public Page<Object[]> getImagelist(Pageable page);
	//이미지서치
	public Page<Object[]> getsearchImagelist(Pageable page,String option,String keyword);
	
	public List<detachfile> getPrevimage(Long noticeid);
	
	public boolean childparuntcount(Long commentid);
	
	public Optional<CommentEntity>  deletecommentget(Long commentid);
	
	public List<CommentEntity> childcomments(Long noticeid,List<Long> parentid);

	long likecounts(Long noticeid);
	
	public Page<TwitformnoticeDto> getuserpagepost(Long loginid,Long searchid,Pageable pageable);
	
	public Page<TwitformnoticeDto> getuserpagepostsearch(Long loginid,Long searchid,String option,String keyword,Pageable pageable);
	
	public Page<Object[]> getuserpageimages(Pageable page,Long userid);
	
	
}
