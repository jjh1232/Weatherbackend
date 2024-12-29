package com.example.firstproject.Entity.oauth2;

import java.util.Map;

import lombok.Data;

@Data
public class Naverprofile implements Provider{

	
	public Map<String,Object> attributes;
	
	public Naverprofile(Map<String,Object> attri) {
		this.attributes=attri;
	}
	
	
	@Override
	public String getProvider() {
		// TODO Auto-generated method stub
		return "Naver";
	}

	@Override
	public String getProviderid() {
		// TODO Auto-generated method stub
		return (String) attributes.get("id");
	}

	@Override
	public String getEmail() {
		// TODO Auto-generated method stub
		return (String) attributes.get("email");
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return (String) attributes.get("name");
	}
	public String getNickname() {
		return (String) attributes.get("Nickname");
	}

	
	
}
