package com.example.firstproject.CustomError;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

	private final HttpStatus status;
	
	private final String errorCode;
	private final String detail;
	

	public CustomException(HttpStatus status, ErrorCode errorCode) {
		super();
		this.status = status;
		this.errorCode = errorCode.getCode();
		this.detail = errorCode.getMsg();
	}



	
	
}
