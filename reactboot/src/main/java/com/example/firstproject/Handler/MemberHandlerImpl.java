package com.example.firstproject.Handler;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.userdataDto.UserDto;
import com.example.firstproject.Dto.userdataDto.UserPageDto;
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
	public boolean emailcheck(String username) {
		// TODO Auto-generated method stub
		
		//long check=memberrepository.emailcheck(username);
		boolean check=memberrepository.existsByUsername(username);
		
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

	@Override
	public Optional<MemberEntity> findbyid(Long userid) {
		// TODO Auto-generated method stub
		Optional<MemberEntity> member=memberrepository.findById(userid);
		return member;
	}

	@Override
	public boolean existsByProfileId(String profileid) {
		// TODO Auto-generated method stub
		
		return memberrepository.existsByProfileid(profileid);
	}

	@Override
	public Optional<MemberEntity> findbyusername(String username) {
		// TODO Auto-generated method stub
		
		return memberrepository.findByUsername(username);
	}

	@Override
	public Optional<UserPageDto> findprofileid(String profileid,Long loginid) {
		// TODO Auto-generated method stub
		Optional<UserPageDto> loginmember=memberrepository.findByProfileid(profileid,loginid);
		return loginmember;
	}

	

	

}
