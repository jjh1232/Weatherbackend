package com.example.firstproject.Dto.follow;

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
public class followlistDto {

	private String username;
	private String nickname;
	private boolean favorite;
	private String profileimg;
	private String profileid;
}
