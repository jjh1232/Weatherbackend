package com.example.firstproject.Dto.follow;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class followlistDto {

	private String username;
	private String nickname;
	private boolean favorite;
}
