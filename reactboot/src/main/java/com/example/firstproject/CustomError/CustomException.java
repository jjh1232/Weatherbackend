package com.example.firstproject.CustomError;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

	private final HttpStatus status;
	
	private final String errorCode;
	private final String message;
	

	public CustomException(HttpStatus status, ErrorCode errorCode) {
		  super();
		this.status = status;
		this.errorCode = errorCode.getCode();
		this.message = errorCode.getMsg();
	}



	
	
}
