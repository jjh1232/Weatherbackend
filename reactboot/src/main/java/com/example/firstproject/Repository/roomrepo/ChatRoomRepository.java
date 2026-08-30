package com.example.firstproject.Repository.roomrepo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Dto.ChatDto.Roomdata.Roomdata;
import com.example.firstproject.Entity.StompRoom.Room;


@Repository
public interface ChatRoomRepository extends JpaRepository<Room, Long> {
	

	Page<Room> findByRoomnameContaining(Pageable page,String roomname);
	
	/* jpql로는 어렵고 nativequery로는 된다고하는데 성능상 3번가져오나이거나 비슷하다고함 대용량아니면
	 * 때문에 그냥 fetchjoin으로 할듯? Dto는 List형을 받기 힘들다
	@Query("select new com.example.firstproject.Dto.ChatDto.Roomdata.Roomdata"
			+ "(r.id,r.roomname,r.createdDate,"
			+ "new com.example.firstproject.Dto.ChatDto.Roomdata.EzmemberDto("
			+ "m.id,m.username,m.nickname,m.profileimg),"
			+ "new com.example.firstproject.Dto.ChatDto.Roomdata.MessageDto("
			+ "c.id,c.MessageType,c.message,c.createdDate,"
			+ "new com.example.firstproject.Dto.ChatDto.Roomdata.EzmemberDto("
			+ "cm.id,cm.username,cm.nickname,cm.profileimg))"
			+ ") from Room r left join r.userlist ul"
			+ "left join ul.member m"
			+ "left join r.chatdata c"
			+ "left join c.member cm"
			+ "where r.id=:roomid"
			)
			*/
	@Query("SELECT DISTINCT r FROM Room r " +
		       "JOIN FETCH r.userlist ul " +
		       "JOIN FETCH ul.member m " +
		       "JOIN FETCH r.chatdata c " +
		       "JOIN FETCH c.member cm " +
		       "WHERE r.id = :roomid")
	Room findbyroomdata(@Param("roomid") Long roomid);
	
	//위의코드는 페치조인떄매 컬렉션을 페치조인으로쓰면 카디널프로덕트가 일어나서 중복메세지가 나옴
	//2개를가져옴 그래서 따로따로 가져오게수정 이게더 비용적으로도좋음
	@Query("SELECT DISTINCT r FROM Room r "
			+ "JOIN FETCH r.userlist ul "
			+ "JOIN FETCH ul.member m "
			+ "WHERE r.id =:roomid")
	Room Roomdetailinfo(@Param("roomid") Long roomid);
	
	
}
