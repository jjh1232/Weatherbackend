package com.example.firstproject.Repository.roomrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.StompRoom.chatmessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<chatmessage,Long> {

}
