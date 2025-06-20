package com.example.firstproject.Utils;

import com.example.firstproject.Entity.Address;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Handler.MemberHandler;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Repository.NoticeRepository;

public class TestDataUtils {

	
	//여러개대비 변수값도넣자
	public static MemberEntity createTestuser(MemberRepository memberrepository,Long variablenum) {
		System.out.println("멤버생성");
		MemberEntity member= MemberEntity.builder()
				.username("testuser"+variablenum+"@naver.com")
				.password("test123test123"+variablenum)
				.nickname("testnick"+variablenum)
				.auth("Y")
				.homeaddress(new Address("서울특별시  종로구  청운효자동","60","127"))
				.build();
		
		return memberrepository.save(member);
		
	}
	//노티스
	public static NoticeEntity createTestnotice(NoticeRepository noticerepo,Long variablenum) {
		System.out.println("노티스생성");
		NoticeEntity notice=NoticeEntity.builder()
				.title("테스트제목"+variablenum)
				.text("테스트내용"+variablenum)
				.noticenick("테스트작성자네임"+variablenum)
				.noticeuser("테스트작성자이메일"+variablenum)
				
				.build();
		
		return noticerepo.save(notice);
				
	}
	
}
