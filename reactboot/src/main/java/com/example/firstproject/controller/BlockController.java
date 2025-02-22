package com.example.firstproject.controller;

import java.util.Map;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.firstproject.Dto.blockDto.NoticeblockDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Service.Blockservice.Blockservice;
import com.example.firstproject.configure.PrincipalDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class BlockController {

	private final Blockservice service;
	
	@PostMapping("/noticeblock")
	public ResponseEntity noticeblock(Authentication authentication,@RequestBody NoticeblockDto dto) {
		PrincipalDetails principal=(PrincipalDetails) authentication.getPrincipal();
		MemberEntity member=principal.getMember();
		//long변환을 모르겠네왜안되는지 integer변환후 롱으로 다시바꿈;
		//map으로 받았는데 reason도추가해야해서 ;;걍 dto로
		
		service.noticeblock(member,dto);
		
		return ResponseEntity.ok("차단완료");
}
	
}
