package com.example.firstproject.CustomError;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ResponseDto <T>{

	//에러처리를위한 Dto
	
	private final Integer code;//1성공 -1실패
	private final String msg;
	private final T data;
	
}
