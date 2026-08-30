package com.example.firstproject.Dto.ChatDto.Roomdata;

import com.example.firstproject.Entity.MemberEntity;

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
public class MeseageDto {

	private Long chatid;
	private Long roomid;
	private String messagetype;
	private String message;
	private String red;
	private EzmemberDto sender;
	
}
