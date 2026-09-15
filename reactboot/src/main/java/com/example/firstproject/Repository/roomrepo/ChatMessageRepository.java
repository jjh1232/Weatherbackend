package com.example.firstproject.Repository.roomrepo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Dto.ChatDto.Roomdata.RoommetaInfo;
import com.example.firstproject.Entity.StompRoom.chatmessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<chatmessage,Long> {
	
	Page<chatmessage> findByMessageContaining(Pageable page,String message);

	/* LEFT: 입장·퇴장 시스템 메시지는 member 가 NULL 이다. 그냥 JOIN 이면 걸러져서
	   목록에는 「입장하셨습니다」 가 뜨는데 방 안은 비어 있었다. */
	@Query("SELECT c FROM chatmessage c "
			+ "LEFT JOIN FETCH c.member m "
			+ "WHERE c.room.id =:roomid ORDER BY c.createdDate ASC")
	List<chatmessage> Roomdetailchatget(@Param("roomid") Long roomid);
	
	//채팅방리스트의 마지막글+안읽은갯수구하기
	/* [2026-09-15] 입장·퇴장 시스템 메시지(member_id NULL)는
	   - 마지막 글(미리보기·시간)에는 **포함**한다 — 방이 막 생겼을 때도 「○○님이 입장하셨습니다」 가 보이게
	   - 안 읽은 수(빨간 알림)에는 **넣지 않는다** — 누가 들어온 걸 읽을거리로 세면 알림이 거짓말이 된다 */
	@Query(value="SELECT m.room_id AS roomid,"
			+ "m.id AS lastMessageId,"
			+ "m.message AS lastMessageContent,"
			+ "m.created_date AS lastMessageCreatedAt," //COALESCE 널이아닌첫번째값 여기선0나올수도
			+ "COUNT(CASE WHEN cm.id > COALESCE(ulr.lastchatid,0) AND cm.member_id IS NOT NULL THEN 1 END) AS unreadCount "
			+ "FROM chatmessage m " //조건 만족시 1을 반환 행을세기때문에 카운트됨
			//위의 m은 이너조인으로 최신 값만 구해짐
			+ "INNER JOIN (SELECT room_id,MAX(id) AS max_id " //각방 가장최신아이디 max_id로구하고 룸아이디도
			+ "FROM chatmessage Where room_id IN (:roomids) "
			+ "GROUP BY room_id) last_msg " //이너조인으로 가장큰아이디값을구하고 이름붙임
			+ "ON m.room_id = last_msg.room_id And m.id= last_msg.max_id "//이너조인조건으로 맥스값구하기
			+ "LEFT JOIN chatmessage cm ON m.room_id = cm.room_id " //방번호 기준으로 다시모든메세지를가져옴 
			//리드카운트를위해다가져와야한다
			+ "LEFT JOIN lastreadchat ulr ON ulr.roomid = m.room_id AND "//마지막채팅아이디조인
			+ "ulr.userid = :userid " //마지막메세지 조인
			+ "where m.room_id IN (:roomids) " //쿼리범위를 조건에맞는방으로
			+ "GROUP BY m.room_id, m.id, m.message,m.created_date,ulr.lastchatid" //카운트집계필요한그룹화컬럼
			,nativeQuery = true)
	List<RoommetaInfo> findLastMessageAndUnreadcount(@Param("userid") Long userid,@Param("roomids") List<Long> roomids);
}
