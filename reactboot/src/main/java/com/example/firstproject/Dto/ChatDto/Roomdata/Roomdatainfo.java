package com.example.firstproject.Dto.ChatDto.Roomdata;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Roomdatainfo {

	private Long roomid;
	private String roomtitle;
	private int membercount;
	private List<ChatlistmemberDto> members;
	
}
