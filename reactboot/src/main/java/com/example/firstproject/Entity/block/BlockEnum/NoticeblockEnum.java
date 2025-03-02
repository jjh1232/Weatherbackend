package com.example.firstproject.Entity.block.BlockEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
//게시글블록이넘
public enum NoticeblockEnum {

	spam("스팸및광고"),   //광고및스팸
	discomfort("불쾌함"), //불쾌감을주는
	violent("폭력적"),// 폭력적
	nsfw("선정적"), //선정적인콘텐츠
	nointerested("관심없음"),//관심없음
	baduser("부적절한유저"), //유저가맘에안듬
	noreason("이유없음"), //이유없음
	etc("기타") //기타
;
	private final String value;

	NoticeblockEnum(String value) {
		// TODO Auto-generated constructor stub
		this.value=value;
	}
	
}
