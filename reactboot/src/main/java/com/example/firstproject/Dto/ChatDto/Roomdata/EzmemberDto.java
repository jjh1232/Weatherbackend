package com.example.firstproject.Dto.ChatDto.Roomdata;

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
public class EzmemberDto {

	private Long userid;
	private String email;
	private String nickname;
	private String profileurl;
}
