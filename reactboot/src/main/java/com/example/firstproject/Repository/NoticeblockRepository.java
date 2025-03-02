package com.example.firstproject.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.firstproject.Entity.block.NoticeblockEntity;

public interface NoticeblockRepository extends JpaRepository<NoticeblockEntity, Long>{

	@Query(value = "select exists (select * from noticeblock_entity where member_id =:userid and noticeid=:noticeid)",nativeQuery = true)
	public Long noticeblockcheck(@Param("userid")Long userid,@Param("noticeid")Long noticeid);

	@Query(value = "select b from NoticeblockEntity b where b.member.id =:userid")
	public List<NoticeblockEntity> userblocklist(Long userid);
	
	@Query(value = "select b.noticeid from NoticeblockEntity b where b.member.id =:userid")
	public List<Long> userblocknoticeid(Long userid);
	
}
