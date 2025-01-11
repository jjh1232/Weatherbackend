package com.example.firstproject.Repository.roomrepo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.StompRoom.chatmessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<chatmessage,Long> {
	
	Page<chatmessage> findByMessageContaining(Pageable page,String message);

}
