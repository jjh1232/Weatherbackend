package com.example.firstproject.configure.auth.provider;

import java.util.Map;

public class Naverprovider implements Provider{

	private Map<String,Object> attributes;
	
	public Naverprovider(Map<String,Object> attributes) {
		this.attributes=attributes;
		
	}
	
	@Override
	public String provider() {
		// TODO Auto-generated method stub
		return "Naver";
	}

	@Override
	public String prividerid() {
		// TODO Auto-generated method stub
		return (String) attributes.get("id");
	}

	@Override
	public String getusername() {
		// TODO Auto-generated method stub
		return  (String) attributes.get("email");
	}

	@Override
	public String getname() {
		// TODO Auto-generated method stub
		return  (String) attributes.get("name");
	}

	@Override
	public String getnickname() {
		return (String) attributes.get("name");
	}
	
}
