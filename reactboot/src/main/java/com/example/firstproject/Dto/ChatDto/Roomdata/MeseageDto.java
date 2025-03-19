package com.example.firstproject.Dto.ChatDto.Roomdata;

import com.example.firstproject.Entity.MemberEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MeseageDto {

	private Long id;
	private String messagetype;
	private String message;
	private String red;
	private MemberEntity sender;
	
}
