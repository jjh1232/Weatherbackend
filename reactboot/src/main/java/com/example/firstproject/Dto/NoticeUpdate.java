package com.example.firstproject.Dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.example.firstproject.Entity.detachfile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeUpdate {
	
	@NotBlank
	private String title;
	
	@NotNull
	private String text;
	
	private List<Detachupdateform> detach;
}
