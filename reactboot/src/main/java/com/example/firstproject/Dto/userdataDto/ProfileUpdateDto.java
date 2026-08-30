package com.example.firstproject.Dto.userdataDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/* 유저페이지의 Edit Profile 전용.

   MemberUpdateDto(정보수정 화면)를 재사용하지 않는 이유:
   그쪽 서비스는 dto.region 을 조건 없이 setHomeaddress 로 덮어쓴다.
   이 화면에는 지역 입력이 없어서 그대로 쓰면 사용자 주소가 null 로 날아간다.
   여기서는 닉네임/소개/이미지만 다루고 지역은 건드리지 않는다. */
@Getter
@Setter
public class ProfileUpdateDto {

	@NotBlank
	@Size(min=3,max=10,message = "닉네임은 세글자에서10글자를 사용해주십시오")
	@Pattern(regexp="^[a-z|A-Z|가-힣]*$",message="한글과 영문만사용해주세요")
	private String name;

	//자기소개. 비워둘 수 있다.
	@Size(max=200,message = "소개는 200자까지 쓸 수 있습니다")
	private String myintro;

	//교체 전 이미지 경로. 새 파일이 올라오면 이걸 지운다.
	private String profileimage;
	private String profilebackground;
}
