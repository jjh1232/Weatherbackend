package com.example.firstproject.Dto.ChatDto.Roomdata;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Roominfo {

private Long roomid;
	
	private String roomname;
	
	private String createred;
	
	private List<EzmemberDto> memberlist;
}
