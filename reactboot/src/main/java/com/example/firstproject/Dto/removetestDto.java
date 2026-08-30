package com.example.firstproject.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class removetestDto {

	private Long id;
	private Long idx;
	
	boolean test;
	
	String url;
}
