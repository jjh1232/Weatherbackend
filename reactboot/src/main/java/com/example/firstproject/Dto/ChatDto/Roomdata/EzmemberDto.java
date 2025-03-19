package com.example.firstproject.Dto.ChatDto.Roomdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EzmemberDto {

	private Long userid;
	private String email;
	private String nickname;
	private String profileurl;
}
