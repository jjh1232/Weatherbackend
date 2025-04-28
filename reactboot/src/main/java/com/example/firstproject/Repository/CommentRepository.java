package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Dto.Comment.Testcom;
import com.example.firstproject.Entity.CommentEntity;


public interface CommentRepository extends JpaRepository<CommentEntity,Long>{

	
	@Query(value="Select * from comment where notice_id =:noticenum",nativeQuery=true)
	List<CommentEntity> findbynoticenum(@Param("noticenum") Long num);
	
	

	Page<CommentEntity> findByUsernameContaining(Pageable page,String username);
	
	Page<CommentEntity> findByNicknameContaining(Pageable page,String nickname);
	
	@Query(value="Select * from comment where notice_id =:noticeid",nativeQuery=true)
	Page<CommentEntity> findByNoticeIdContaining(Pageable page,Long noticeid);
	
	//fetch조인은 다가져와서 별로고 직접매핑으로필요한것만 클래스명으로 작성해야함
	@Query(value = "SELECT new com.example.firstproject.Dto.Comment.CommentDto" +
		       "(c.id,n.id,"
		       + "c.depth,c.cnum,"
		       + "m.username,m.nickname,"
		       + "c.text,c.createdDate,m.profileimg) " +   //연관객체도  
		       "FROM CommentEntity c JOIN c.notice n "
		       + "JOIN c.member m "
		       + "where n.id=:noticeid")
	Page<CommentDto> showcomments(Pageable pageable,Long noticeid);//,Long noticeid);
	
}