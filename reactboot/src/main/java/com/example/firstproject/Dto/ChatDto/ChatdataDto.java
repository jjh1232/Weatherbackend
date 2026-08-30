package com.example.firstproject.Dto.ChatDto;

import java.util.List;

import com.example.firstproject.Dto.ChatDto.Roomdata.MeseageDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatdataDto {

	private List<MeseageDto> chatdates;
	private Long lastreadchatid;
}
