package com.example.firstproject.Handler;

import java.util.List;
import java.util.Optional;

import com.example.firstproject.Entity.MemberEntity;

public interface MemberHandler {
	
	public Optional<MemberEntity> findbyid(Long userid);
	
	
	public MemberEntity membercreate(MemberEntity entity);

	public boolean emailcheck(String email);
	
	
	
	public Optional<MemberEntity> login(String email);
	
	public void passwordupdate(String email,String password);
	
	public Optional<MemberEntity> findemail(String email);
	
	public void deletemember(MemberEntity entity);

	public List<MemberEntity> findbynickname(String keyword);
	
	public boolean existsByProfileId(String profileid);


	public Optional<MemberEntity> findbyusername(String username);
}
