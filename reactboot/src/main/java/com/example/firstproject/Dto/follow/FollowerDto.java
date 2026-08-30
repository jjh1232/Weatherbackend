package com.example.firstproject.Dto.follow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowerDto {

	private String username;
	private String nickname;
	private String profileimg;
	private String profileid;
	private boolean followcheck;
}
