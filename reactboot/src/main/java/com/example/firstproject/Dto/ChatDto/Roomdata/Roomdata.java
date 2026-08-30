package com.example.firstproject.Dto.ChatDto.Roomdata;

import java.util.List;

import com.example.firstproject.Dto.MessageDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Roomdata {

	private Long roomid;
	
	private String roomname;
	
	private String createred;
	
	private List<EzmemberDto> memberlist;
	


	private List<MeseageDto> chatdata;
	

}
