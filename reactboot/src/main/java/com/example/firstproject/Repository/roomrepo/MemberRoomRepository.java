package com.example.firstproject.Repository.roomrepo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.firstproject.Dto.ChatDto.Roomdata.ChatlistmemberDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.EzmemberDto;
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
	//============================기존룸리스트============================================
	//멤버룸객체가져오기
	@Query("select mr FROM MemberRoom mr JOIN FETCH mr.room where mr.member.id=:memberid")
	List<MemberRoom> findmemberroomlist(@Param("memberid") Long memberid);

	
	//각방멤버리스트가져오기
	@Query("SELECT mr FROM MemberRoom mr JOIN FETCH mr.member where mr.room.id IN :roomids")
	List<MemberRoom> findMemberRoomsbyroomid(@Param("roomids") List<Long> roomids);
	//========================리펙토링=================================================
	//비용줄이기 룸아이디만가져옴 이거복합키로할껄..
	@Query("SELECT DISTINCT mr.room.id FROM MemberRoom mr where mr.member.id=:memberid")
	List<Long> findmemberroomidbymemberids(@Param("memberid") Long memberid);
	//dto프로덕션으로 가져오자 
	@Query("SELECT new com.example.firstproject.Dto.ChatDto.Roomdata.ChatlistmemberDto("
			+ "mr.member.id,mr.room.id,mr.member.username,mr.member.nickname,mr.member.profileimg) "
			+ "FROM MemberRoom mr where mr.room.id IN :roomids")
	List<ChatlistmemberDto> findmemberroomsbyroomids(@Param("roomids") List<Long> roomids);
	

}
