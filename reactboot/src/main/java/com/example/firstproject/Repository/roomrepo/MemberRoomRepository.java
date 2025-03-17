package com.example.firstproject.Repository.roomrepo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.StompRoom.MemberRoom;


public interface MemberRoomRepository extends JpaRepository<MemberRoom, Long>{

	Optional<MemberRoom> findByRoom_idAndMember_Id(Long roomid,Long memberid);
	
	Page<MemberRoom> findByMembernickname(Pageable page,String membernickname);
	
	Page<MemberRoom> findByMember(Pageable page,MemberEntity member);
	
	@Query("SELECT DISTINCT mr FROM MemberRoom mr " +
		       "JOIN mr.room r " +
		       "JOIN mr.member m " +
		       "WHERE r.id IN (SELECT mr2.room.id FROM MemberRoom mr2 WHERE mr2.member.id = :memberid)")
	List<MemberRoom> findMemberrooms(@Param("memberid") Long memberid);
	
}
