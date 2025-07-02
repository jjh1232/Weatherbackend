package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.FavoriteEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;

@Repository

public interface LikeRepository extends JpaRepository<FavoriteEntity,Long> {

	
	//엔티티가져와야해서비효율적인듯?
	Optional<FavoriteEntity> findByNoticeAndMember(NoticeEntity notice,MemberEntity member);
	
	
	Optional<FavoriteEntity> findByNoticeNoticeidAndMemberId(Long noticeId, Long memberId);
	
	Page<FavoriteEntity> findByMember(MemberEntity member,Pageable pageable);
	
	//@Query(value = "select f.noticeid from FavoriteEntity f where f.member.id =:userid")
	//public List<Long> userblocknoticeid(Long userid);
	
	//특정쿼리만 받으려면 jpa문으론힘듬 때문에 쿼리사용
	@Query(value= "select f.notice.id from FavoriteEntity f where f.member.id=:userid and f.notice.id in :noticeids")
	public List<Long> findfavoriteids(Long userid,List<Long>noticeids);

	//noticeid를 noticeid로한탓에..
	long countByNoticeNoticeid(Long noticeid);
}
