package com.example.firstproject.Dto;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Memberform {

	//@NotNull(message= "비어있습니다")//공백과빈문자열허용
	//@NotEmpty(message="문자열이비었어")  //빈문자열불가능공백은가능
	////@Max(30) Min 과 Max 는 숫자형만적용이된다고함 ;
	@NotBlank (message="비었어") //빈문자 스페이스있는거안되
	@Email(message="이메일형식이아닙니다")
	@Size(min=8,max=50)
	private String username;
	
	//@NotNull
	//@NotEmpty
	@NotBlank
	@Pattern(regexp ="^(?=.*[a-zA-Z])((?=.*\\d)(?=.*\\W)).{8,16}+$", message = "비밀번호는 8~16자 영문 , 숫자, 특수문자를 사용하세요.")//정규식
	private String password;
	//@NotNull
	//@NotEmpty
	
	@NotBlank
	@Size(min=3,max=16,message = "프로필아이디는 세글자에서16글자를 사용해주십시오")
	@Pattern(regexp = "^[a-zA-Z가-힣0-9]*$", message = "한글, 영문 또는 숫자만 입력 가능합니다.")
	private String profileid;

	@NotBlank
	@Size(min=3,max=10,message = "닉네임은 세글자에서10글자를 사용해주십시오")
	@Pattern(regexp="^[a-z|A-Z|가-힣]*$",message="한글과 영문만사용해주세요")
	private String nickname;
	
	
	
	private String region;
	private String gridx;
	private String gridy;

	//──────────────────────────────────────────────────────────
	// 필수 동의 (개인정보 보호법 제15조)
	//  화면에서 체크박스를 막아도 요청은 직접 만들어 보낼 수 있다.
	//  동의 없이 개인정보를 저장하면 안 되므로 서버에서 한 번 더 막는다.
	//  @AssertTrue 는 값이 true 가 아니면 400 으로 떨어뜨린다.
	//──────────────────────────────────────────────────────────
	@AssertTrue(message="이용약관에 동의해야 가입할 수 있습니다.")
	private boolean agreeterms;

	@AssertTrue(message="개인정보 수집·이용에 동의해야 가입할 수 있습니다.")
	private boolean agreeprivacy;

	@AssertTrue(message="만 14세 이상만 가입할 수 있습니다.")
	private boolean agreeage;
	
	
}
