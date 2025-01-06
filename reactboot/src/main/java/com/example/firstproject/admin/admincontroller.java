package com.example.firstproject.admin;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Entity.MemberEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class admincontroller {

	private final adminService adminservice;
	
	@GetMapping("/main")
	public ResponseEntity getmain() {
		
		
		return null;
	}
	
	@GetMapping("/membermanage")
	public ResponseEntity memberpage(@RequestParam(defaultValue = "1") int page) {
		
		System.out.println("어드민시작");
		Page<MemberDto> memberlist=adminservice.allmemberget(page);
		System.out.println("데이터전달");
		
		return ResponseEntity.ok(memberlist);
		
	}
}
