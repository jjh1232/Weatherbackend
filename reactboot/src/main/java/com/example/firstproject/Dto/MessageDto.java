package com.example.firstproject.Dto;

import java.util.Map;

import javax.persistence.Entity;

import org.springframework.web.bind.annotation.RequestMethod;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageDto {

	
	private String message;
	private String redirectUri;
	private RequestMethod method; //http요청메소드
	private Map<String,Object> data; //화면으로전달데이터
}
