package com.example.firstproject.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.FavoriteEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;

@Repository
public interface LikeRepository extends JpaRepository<FavoriteEntity,Long> {

	
	
	Optional<FavoriteEntity> findByNoticeAndMember(NoticeEntity notice,MemberEntity member);
	
	
}
