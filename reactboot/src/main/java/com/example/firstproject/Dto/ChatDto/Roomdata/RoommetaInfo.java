package com.example.firstproject.Dto.ChatDto.Roomdata;

/**
 * 채팅방 목록의 「마지막 메시지 + 안 읽은 수」 (네이티브 쿼리 projection).
 *
 * <p>[2026-09-15] lastMessageCreatedAt 을 LocalDateTime 으로 받고 있었는데
 * chatmessage.created_date 는 문자열(yyyy.MM.dd/HH:mm:ss)이다. JSON 으로 쓸 때 변환이 터져서
 * 메시지가 하나라도 있는 방이 목록에 섞이면 /findchatroommeta 가 통째로 실패했다
 * (방을 만들면 입장 시스템 메시지가 생기므로 사실상 항상).
 * 프론트(Datefor)는 이 문자열을 그대로 new Date() 로 읽는다. 글 목록 시간도 같은 형식이다.
 */
public interface RoommetaInfo {
	Long getRoomid();
	Long getLastMessageId();
	String getLastMessageContent();
	String getLastMessageCreatedAt();
	Long getunreadCount();
}
