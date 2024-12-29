package com.example.firstproject.Handler;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Repository.MemberRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MemberHandlerImpl implements MemberHandler{

	@Autowired
	MemberRepository memberrepository;

	@Override
	public MemberEntity membercreate(MemberEntity entity) {
		// TODO Auto-generated method stub
		
		memberrepository.save(entity);
		return entity;
	}

	@Override
	public Long emailcheck(String username) {
		// TODO Auto-generated method stub
		String as="as";
		log.info(as);
		long check=memberrepository.emailcheck(username);
		String a=String.valueOf(check);
		log.info(a);
		return check;
	}

	@Override
	public Optional<MemberEntity> login(String username) {
		// TODO Auto-generated method stub
		Optional<MemberEntity> loginmember=memberrepository.findByUsername(username);
		
		return loginmember;
	}

	@Override
	public void passwordupdate(String username,String password) {
			memberrepository.passwordupdate(username,password);
	
	}

	@Override
	public Optional<MemberEntity> findemail(String username) {
		// TODO Auto-generated method stub
		Optional<MemberEntity> data=memberrepository.findByUsername(username);
		return data;
	}

	@Override
	public void deletemember(MemberEntity entity) {
		// TODO Auto-generated method stub
		memberrepository.delete(entity);
		
		
		
	}

	@Override
	public List<MemberEntity> findbynickname(String keyword) {
		// TODO Auto-generated method stub
		List<MemberEntity> list=memberrepository.findByNicknameContaining(keyword);
		return list;
	}
	
	

}
