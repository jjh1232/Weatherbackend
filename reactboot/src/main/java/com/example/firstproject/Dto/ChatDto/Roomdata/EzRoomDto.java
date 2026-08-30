package com.example.firstproject.Dto.ChatDto.Roomdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class EzRoomDto {

	private Long roomid;
	
	private String roomname;
}
