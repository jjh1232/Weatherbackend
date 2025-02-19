package com.example.firstproject.controller;

import java.util.Map;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.firstproject.configure.PrincipalDetails;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BlockController {

	
	@PostMapping("/noticeblock")
	public ResponseEntity noticeblock(Authentication authentication,@RequestBody Map<String,Object> data) {
		PrincipalDetails principal=(PrincipalDetails) authentication.getPrincipal();
		
		Long noticeid=(Long) data.get("noticeid");
		
		return ResponseEntity.ok("da");
}
}
