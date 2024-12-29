package com.example.firstproject.controller;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

import ch.qos.logback.core.status.Status;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ErrorController {

	//Valid에러 인지 Duply에러인지 확인하고 exception에서 정보받아서 필요한거 매핑해서주거나 그냥주면될듯?
	@ExceptionHandler(MethodArgumentNotValidException.class)//바인드써야함
	public ResponseEntity validerror(MethodArgumentNotValidException exception,HttpServletRequest req) {
		BindingResult bindresult=exception.getBindingResult();//메서드아구먼트는 여기서
		String message= bindresult.getFieldError().getDefaultMessage().toString();
		System.out.println(bindresult);
		System.out.println(bindresult.getErrorCount());
		System.out.println(bindresult.getFieldError().getDefaultMessage().toString());
		System.out.println("url:"+req.getRequestURI());//요청정보에서필요한데이터가져오기
		return new ResponseEntity<>(message,HttpStatus.BAD_REQUEST);
	}
	
	//@ExceptionHandler(RuntimeException.class) //sql 에러 이걸로나네 일단닉네임중복만
	public	ResponseEntity runtimerror(RuntimeException exception) {
		String message=exception.getMessage(); //런타임은바로잡힘..
		String nicknamemessage="동일한닉네임이존재합니다!다른닉네임을 작성해주세요!";
		System.out.println("bind:"+message);
		return new ResponseEntity<>(nicknamemessage,HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(SQLException.class)
	public ResponseEntity sqlerror(SQLException exception) {
		String message=exception.getMessage();
		System.out.println(message);
				return new ResponseEntity<>(message,HttpStatus.BAD_REQUEST);
	}

	
}
