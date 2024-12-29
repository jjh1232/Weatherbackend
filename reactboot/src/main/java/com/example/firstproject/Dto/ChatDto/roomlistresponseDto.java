package com.example.firstproject.Dto.ChatDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.example.firstproject.Entity.StompRoom.MemberRoom;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class roomlistresponseDto {
	
	private Long roomid;
	
	private String roomname;
	
	private Set<MemberRoom> namelist;
	
	private LocalDateTime time;
	
	private String latelychat;
}
