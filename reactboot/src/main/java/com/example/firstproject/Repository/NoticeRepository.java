package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.NoticeDtointer;
import com.example.firstproject.Entity.NoticeEntity;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long>{
	
	//====================================서브쿼리연습용=======================================
	@Query(value="Select * from notice order by red DESC limit :start,:end",nativeQuery = true)
	List<NoticeEntity> findnotice(@Param("start") int start,@Param("end") int end);
	
	@Query(value="Select n.*,m.profileimg,d.path from notice as n "
			+ "left join detachfiles as d on n.id=d.notice_id "
			+ "left join member as m on m.id=n.member_id "
					
	
			,nativeQuery = true)
	Page<Object> test113(Pageable pageable);
	//List<NoticeEntity> findByredDESC(Pageable page);
	
	
	//=====================================일단 eager이라도써서돌아가게============================================
	//Page<NoticeEntity> findAll(Pageable pageable);
	//jpa는on지원안한다고함
	//@Query(value="select n.* from notice as n join member as m on m.id=n.member_id where n.text like %:text%",nativeQuery = true)
	@Query(value="Select * from notice   where title like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchtitle(@Param("text") String text,Pageable pageable);
	
	
	//@Query(value="select n.* from notice n join fetch n.member m where m.id=n.member_id where n.text like %:text%")
	@Query(value="Select * from notice   where text like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchtext(@Param("text") String text,Pageable pageable	);
	
	//@Query(value="select n.* from notice n join fetch n.member m where m.id=n.member_id where n.nickname like %:text%")
	@Query(value="Select * from notice   where nickname like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchname(@Param("text") String text,Pageable pageable	);
	
	//@Query(value="select n.* from notice n join fetch n.member m where m.id=n.member_id where n.title like %:text% or text like %:text%")
	@Query(value="Select * from notice where text like %:text% or title like %:text% ",nativeQuery=true)
	Page<NoticeEntity> searchtitletext(@Param("text") String text,Pageable pageable);
	
	//@Query(value="select n.* from notice n join fetch n.member m where m.id=n.member_id where n.nickname like %:text%")
	@Query(value="Select * from notice  where nickname like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchnoticeex(@Param("text") String text,Pageable page);
}
