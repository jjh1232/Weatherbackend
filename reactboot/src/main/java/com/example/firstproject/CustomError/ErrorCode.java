package com.example.firstproject.CustomError;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	//400
	UNKNOWN("000_UNKOWN","알수없는에러가발생했습니다"),
	NOT_ALLOW_EMAIL("009_NOT_ALLOW_EMAIL","인증되지 않은이메일입니다!"),
	
	//401
	INVALID_TOKEN("101_INVALID_TOKEN","유효하지 않은 토큰입니다");
	
	private final String code;
	private final String msg;
}
