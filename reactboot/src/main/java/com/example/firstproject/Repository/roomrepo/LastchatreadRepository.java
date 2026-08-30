package com.example.firstproject.Repository.roomrepo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.UserChatrecord.LastReadId;
import com.example.firstproject.Entity.UserChatrecord.chatrecord;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface LastchatreadRepository extends JpaRepository<chatrecord, LastReadId>{

	Optional<chatrecord> findById(LastReadId id);
	//엔티티명사용;; 복합키라 사용도유의
	@Query("Select cr from chatrecord cr where cr.id.userid =:userid and cr.id.roomid in :roomids ")
	List<chatrecord> findlastchatIds(@Param("userid") Long userid,@Param("roomids") List<Long> roomids);
}
