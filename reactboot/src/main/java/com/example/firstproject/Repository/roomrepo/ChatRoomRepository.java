package com.example.firstproject.Repository.roomrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.StompRoom.Room;


@Repository
public interface ChatRoomRepository extends JpaRepository<Room, Long> {
	

}
