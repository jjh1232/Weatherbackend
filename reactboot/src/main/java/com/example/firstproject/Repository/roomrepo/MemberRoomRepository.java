package com.example.firstproject.Repository.roomrepo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.firstproject.Entity.StompRoom.MemberRoom;

public interface MemberRoomRepository extends JpaRepository<MemberRoom, Long>{

	Optional<MemberRoom> findByRoom_idAndMember_Id(Long roomid,Long memberid);
}
