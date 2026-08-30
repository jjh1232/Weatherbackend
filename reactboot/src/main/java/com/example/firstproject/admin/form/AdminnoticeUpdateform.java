package com.example.firstproject.admin.form;

import java.util.List;

import com.example.firstproject.Dto.Detachupdateform;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminnoticeUpdateform {

	private String username;
	
	private String nickname;
	
	private String title;
	
	private String text;
	
	private String temp;
	
	private String sky;
	
	private String pty;
	
	private String rain;
	
	private List<Detachupdateform> files;
}
