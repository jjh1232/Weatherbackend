package com.example.firstproject.Handler;

import java.util.List;
import java.util.Optional;

import com.example.firstproject.Entity.MemberEntity;

public interface MemberHandler {

	public MemberEntity membercreate(MemberEntity entity);

	public Long emailcheck(String email);
	
	public Optional<MemberEntity> login(String email);
	
	public void passwordupdate(String email,String password);
	
	public Optional<MemberEntity> findemail(String email);
	
	public void deletemember(MemberEntity entity);

	public List<MemberEntity> findbynickname(String keyword);
}
