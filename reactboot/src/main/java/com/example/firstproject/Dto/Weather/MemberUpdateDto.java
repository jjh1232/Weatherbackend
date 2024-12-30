package com.example.firstproject.Dto.Weather;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateDto {

	@NotBlank
	@Size(min=3,max=10,message = "닉네임은 세글자에서10글자를 사용해주십시오")
	@Pattern(regexp="^[a-z|A-Z|가-힣]*$",message="한글과 영문만사용해주세요")
	public String name;
	
	public String email;
	
	public String profileimage;
	
	private String region;
	private String gridx;
	private String gridy;
	
}
