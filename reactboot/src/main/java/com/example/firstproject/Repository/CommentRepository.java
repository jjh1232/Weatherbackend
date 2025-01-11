package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.firstproject.Entity.CommentEntity;


public interface CommentRepository extends JpaRepository<CommentEntity,Long>{

	
	@Query(value="Select * from comment where notice_id =:noticenum",nativeQuery=true)
	List<CommentEntity> findbynoticenum(@Param("noticenum") Long num);


	Page<CommentEntity> findByUsernameContaining(Pageable page,String username);
	
	Page<CommentEntity> findByNicknameContaining(Pageable page,String nickname);
	
	@Query(value="Select * from comment where notice_id =:noticeid",nativeQuery=true)
	Page<CommentEntity> findByNoticeIdContaining(Pageable page,Long noticeid);
	
}