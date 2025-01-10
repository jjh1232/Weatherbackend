package com.example.firstproject.admin.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;

@Getter
public class Admemberupdateform {

	
	//@NotNull(message= "비어있습니다")//공백과빈문자열허용
			//@NotEmpty(message="문자열이비었어")  //빈문자열불가능공백은가능
			////@Max(30) Min 과 Max 는 숫자형만적용이된다고함 ;
			@NotBlank (message="비었어") //빈문자 스페이스있는거안되
			@Email(message="이메일형식이아닙니다")
			@Size(min=8,max=50)
			private String username;
			
			
			

			@NotBlank
			@Size(min=3,max=10,message = "닉네임은 세글자에서10글자를 사용해주십시오")
			@Pattern(regexp="^[a-z|A-Z|가-힣]*$",message="한글과 영문만사용해주세요")
			private String nickname;
			@NotBlank
			private String provider;
			@NotBlank
			private String role;
			private String region;
			private String gridx;
			private String gridy;
}
