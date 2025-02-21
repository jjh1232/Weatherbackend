package com.example.firstproject.Entity.block.BlockEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
//게시글블록이넘
public enum NoticeblockEnum {

	spam,   //광고및스팸
	discomfort, //불쾌감을주는
	violent,// 폭력적
	nsfw, //선정적인콘텐츠
	nointerested,//관심없음
	baduser, //유저가맘에안듬
	noreason, //이유없음
	etc //기타
	
}
