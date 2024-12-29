package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.NoticeEntity;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long>{
	
	@Query(value="Select * from notice order by red DESC limit :start,:end",nativeQuery = true)
	List<NoticeEntity> findnotice(@Param("start") int start,@Param("end") int end);
	
	//List<NoticeEntity> findByredDESC(Pageable page);
	
	//Page<NoticeEntity> findAll(Pageable pageable);
	
	@Query(value="Select * from notice   where title like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchtitle(@Param("text") String text,Pageable pageable);
	
	@Query(value="Select * from notice   where text like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchtext(@Param("text") String text,Pageable pageable	);
	
	@Query(value="Select * from notice   where nickname like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchname(@Param("text") String text,Pageable pageable	);
	
	@Query(value="Select * from notice where text like %:text% or title like %:text% ",nativeQuery=true)
	Page<NoticeEntity> searchtitletext(@Param("text") String text,Pageable pageable);
	
	@Query(value="Select * from notice  where nickname like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchnoticeex(@Param("text") String text,Pageable page);
}
