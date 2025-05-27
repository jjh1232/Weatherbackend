package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

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
	
	
	//유저블록 가져오기 
	
	@Query(value="select b from NoticeblockEntity b where b.member.id =:memberid and noticeid=:noticeid")
	public Optional<NoticeblockEntity> findbymemberidandnoticeid(Long memberid,Long noticeid);

	
	@Query(value="select b from NoticeblockEntity b where b.member.id =:memberid and noticeid=:noticeid")
	public boolean isblockcheck(Long memberid,Long noticeid);

}
