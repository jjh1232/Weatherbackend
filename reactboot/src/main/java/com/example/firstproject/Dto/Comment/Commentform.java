package com.example.firstproject.Dto.Comment;

import javax.validation.constraints.NotBlank;

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
public class Commentform {

	
	@NotBlank
	private Long noticeid;
	
	
	@NotBlank
	private int depth;
	
	private int cnum;
	

	@NotBlank
	private String username;
	@NotBlank
	private String nickname;
	@NotBlank
	private String text;
	
}
