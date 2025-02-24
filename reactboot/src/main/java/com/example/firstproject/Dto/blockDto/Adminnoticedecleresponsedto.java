package com.example.firstproject.Dto.blockDto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Adminnoticedecleresponsedto {

	private String username;
	private long noticeid;
	private String reason;

	private String datetime;
}
