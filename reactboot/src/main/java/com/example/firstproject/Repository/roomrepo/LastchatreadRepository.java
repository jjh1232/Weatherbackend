package com.example.firstproject.Repository.roomrepo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.firstproject.Entity.UserChatrecord.LastReadId;
import com.example.firstproject.Entity.UserChatrecord.chatrecord;

public interface LastchatreadRepository extends JpaRepository<chatrecord, LastReadId>{

	Optional<chatrecord> findById(LastReadId id);

}
