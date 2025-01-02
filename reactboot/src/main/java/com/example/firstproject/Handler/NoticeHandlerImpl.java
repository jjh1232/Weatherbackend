package com.example.firstproject.Handler;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.FavoriteEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.detachfile;
import com.example.firstproject.Repository.CommentRepository;
import com.example.firstproject.Repository.DetachfileRepository;
import com.example.firstproject.Repository.LikeRepository;
import com.example.firstproject.Repository.NoticeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeHandlerImpl implements NoticeHandler{

	@Autowired
	private NoticeRepository noticerepository;
	
	@Autowired
	private CommentRepository commentrepository;
	
	private final DetachfileRepository detachrepo;
	
	private final LikeRepository likerepository;
		
	@Override
	public Page<NoticeEntity> read(Pageable page) {
		System.out.println("페이지핸들러");
		Page<NoticeEntity> entity=noticerepository.findAll(page);
		return entity;
	}


	
	  @Override
	  public List<NoticeEntity> readfd(int page) {
		  int start =(page-1)*10;
		  int end = 10;
	  List<NoticeEntity> entity = noticerepository.findnotice(start,end);
			  return entity; }



	@Override
	public void create(NoticeEntity entity) {
		// TODO Auto-generated method stub
		noticerepository.save(entity);
	}



	@Override
	public void delete(Long num) {
		// TODO Auto-generated method stub
		noticerepository.deleteById(num);
	}
	
	
	@Override
	public NoticeEntity update(NoticeEntity entity) {
		noticerepository.save(entity);
	
		return entity;
	}



	@Override
	public Optional<NoticeEntity> findbyId(Long num) {
		// TODO Auto-generated method stub
		Optional<NoticeEntity> find=noticerepository.findById(num);
		
		
		
		return find;
	}



	@Override
	public NoticeEntity detail(Long num) {
		NoticeEntity Entity =noticerepository.getById(num);
		
		return Entity;
	}



	@Override
	public void commentcreate(CommentEntity entity) {
		
		commentrepository.save(entity);
		
	}



	@Override
	public List<CommentEntity> commentget(Long num) {
		// TODO Auto-generated method stub
		List<CommentEntity> find=commentrepository.findbynoticenum(num);
		return find;
	}



	@Override
	public Page<NoticeEntity> searchtitle(String text,Pageable pageable) {
		// TODO Auto-generated method stub
		log.info("핸들러입갤");
		Page<NoticeEntity> result=noticerepository.searchtitle(text,pageable);
		
		
		return result;
	}



	@Override
	public Page<NoticeEntity> searchtitletext(String text,Pageable pageable) {
		
		Page<NoticeEntity> result=noticerepository.searchtitletext(text,pageable);
		return result;
	}



	@Override
	public Page<NoticeEntity> searchtext(String text,Pageable pageable) {
			Page<NoticeEntity> result=noticerepository.searchtext(text,pageable);
			
		
		return result;
	}



	@Override
	public Page<NoticeEntity> searchname(String text,Pageable pageable) {
			Page<NoticeEntity> result=noticerepository.searchname(text,pageable);
		
		
		return result;
	}



	@Override
	public Optional<CommentEntity> findcomment(Long id) {
		// TODO Auto-generated method stub
			Optional<CommentEntity> comment =commentrepository.findById(id);
		return comment;
	}



	@Override
	public void deletecomment(Long id) {
		// TODO Auto-generated method stub
		commentrepository.deleteById(id);
	}



	@Override
	public List<detachfile> getdatachfiles(String path) {
		// TODO Auto-generated method stub
		List<detachfile> file=detachrepo.findByPathContaining(path);//데이터날짜로찾자
		return file;
	}



	@Override
	public Optional<FavoriteEntity>  findbynoticeanduser(MemberEntity member, NoticeEntity notice) {
		// TODO Auto-generated method stub
		log.info("좋아요핸들러");
		Optional<FavoriteEntity> found=likerepository.findByNoticeAndMember(notice,member);
		
		return found;
	
	}



	



	@Override
	public void favoritesave(FavoriteEntity favorite) {
		// TODO Auto-generated method stub
		log.info("좋아요핸들러");
		likerepository.save(favorite);
	}



	@Override
	public void favoritedelete(FavoriteEntity favorite) {
		// TODO Auto-generated method stub
		log.info("좋아요해제핸들러");
		likerepository.delete(favorite);
	}



	@Override
	public Page<FavoriteEntity> favoritenoticefind(MemberEntity member,Pageable pageable) {
		// TODO Auto-generated method stub
		Page<FavoriteEntity> followentity=likerepository.findByMember(member, pageable);
		return followentity;
	}
	 

}
