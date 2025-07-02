package com.example.firstproject.Repository.roomrepo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.StompRoom.chatmessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<chatmessage,Long> {
	
	Page<chatmessage> findByMessageContaining(Pageable page,String message);

	@Query("SELECT c FROM chatmessage c "
			+ "JOIN FETCH c.member m "
			+ "WHERE c.room.id =:roomid ORDER BY c.createdDate ASC")
	List<chatmessage> Roomdetailchatget(@Param("roomid") Long roomid);
}
