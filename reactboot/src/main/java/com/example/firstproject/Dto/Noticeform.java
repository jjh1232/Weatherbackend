package com.example.firstproject.Dto;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.example.firstproject.Entity.detachfile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Noticeform {

	
	
	@NotBlank (message="비었어") //빈문자 스페이스있는거안되
	@Email(message="이메일형식이아닙니다")
	@Size(min=8,max=50)
	private String username;
	
	@NotBlank (message="비었어") //빈문자 스페이스있는거안되
	private String nickname;
	
	@NotBlank (message="비었어") //빈문자 스페이스있는거안되
	private String title;
	
	@NotBlank (message="비었어") //빈문자 스페이스있는거안되
	private String text;
	
	private String temp;
	private String sky;
	private String pty;
	private String rain;
	private List<datachfiledto> files;
}
