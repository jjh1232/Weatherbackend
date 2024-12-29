package com.example.firstproject.configure.auth.provider;

import java.util.Map;

public class Googleprovider implements Provider{

	Map<String,Object> user;
	
	public Googleprovider(Map<String,Object> attributes) {
		this.user=attributes;
	}
	
	@Override
	public String provider() {
		// TODO Auto-generated method stub
		return "Google";
	}

	@Override
	public String prividerid() {
		// TODO Auto-generated method stub
		return (String) user.get("sub");
	}

	@Override
	public String getusername() {
		// TODO Auto-generated method stub
		return (String) user.get("email");
	}

	@Override
	public String getname() {
		// TODO Auto-generated method stub
		return (String) user.get("name");
	}

	@Override
	public String getnickname() {
		// TODO Auto-generated method stub
		return (String) user.get("nickname");
	}

}
