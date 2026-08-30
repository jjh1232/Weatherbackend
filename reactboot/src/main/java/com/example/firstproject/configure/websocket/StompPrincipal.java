package com.example.firstproject.configure.websocket;

import java.security.Principal;

public class StompPrincipal implements Principal{

	private final String userid;
	
	public StompPrincipal(String userid) {
		this.userid=userid;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return userid;
	}


}
