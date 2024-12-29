package com.example.firstproject.Dto.ChatDto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Roomuseradd {

	private Long roomid;
	private List<String> userlist;
}
