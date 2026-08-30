package com.example.firstproject.Dto.blockDto;

import java.util.List;
import java.util.Set;

import com.example.firstproject.Entity.block.BlockEnum.NoticeblockEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticedecleDto {

private Long noticeid;
	
	private List<String> reason;
	
}
