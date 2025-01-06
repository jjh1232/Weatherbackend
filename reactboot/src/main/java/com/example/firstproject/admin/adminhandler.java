package com.example.firstproject.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class adminhandler {

	private final MemberRepository memberrepo;
	
	public Page<MemberEntity> memberlistget(Pageable page){
		System.out.println("핸들러시작");
		Page<MemberEntity> memberlist=memberrepo.findAll(page);		
		System.out.println("리포종료");
		return memberlist;
		
	}
}
