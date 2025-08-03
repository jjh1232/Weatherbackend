package com.example.firstproject.Dto.ChatDto.Roomdata;

import java.time.LocalDateTime;

public interface RoommetaInfo {

	Long getRoomid();
	Long getLastMessageId();
	String getLastMessageContent();
	LocalDateTime getLastMessageCreatedAt();
	Long getunreadCount();
}
