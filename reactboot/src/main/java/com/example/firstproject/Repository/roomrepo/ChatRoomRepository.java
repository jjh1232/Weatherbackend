package com.example.firstproject.Repository.roomrepo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.StompRoom.Room;


@Repository
public interface ChatRoomRepository extends JpaRepository<Room, Long> {
	

	Page<Room> findByRoomnameContaining(Pageable page,String roomname);
	
	
}
