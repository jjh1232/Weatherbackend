package com.example.firstproject.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Service.SecurityService;
import com.example.firstproject.configure.PrincipalDetails;
import com.example.firstproject.configure.auth.authenticationfilter;

@RestController
@RequestMapping("/security")
@CrossOrigin("*")
public class Securityex {
	@Autowired
	private SecurityService securityservice;
	
	@GetMapping("/create/token")  //원래는포스트로 아이디나패스워드릉던짐
	public Map<String,Object> createToken(@RequestParam(value="subject") String subject){
		String token =securityservice.createToken(subject, (2*1000*60));
		Map<String,Object>map=new LinkedHashMap<>();
		map.put("result", token);
		return map;
		
	}
	@GetMapping("/get/subject")
	public Map<String,Object> getsubject(@RequestParam(value="token") String token){
		String subject =securityservice.getsubject(token);
		Map<String,Object>map=new LinkedHashMap<>();
		map.put("result", subject);
		return map;
		
	}
	

	
	

}

