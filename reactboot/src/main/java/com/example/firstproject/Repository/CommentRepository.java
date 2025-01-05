package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.firstproject.Entity.CommentEntity;


public interface CommentRepository extends JpaRepository<CommentEntity,Long>{

	
	@Query(value="Select * from comment where notice_id =:noticenum",nativeQuery=true)
	List<CommentEntity> findbynoticenum(@Param("noticenum") Long num);
}
