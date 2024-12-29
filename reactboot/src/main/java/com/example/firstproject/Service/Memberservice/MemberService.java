package com.example.firstproject.Service.Memberservice;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.Memberform;
import com.example.firstproject.Dto.follow.findDto;
import com.example.firstproject.Entity.MemberEntity;

public interface MemberService {

	public int auth(String username,String authokey);
	
	public MemberDto membercreate(Memberform form);

	public long findbyemail(String username);

	public String Emailauth(String username);
	
	public Object memberlogin(String email,String password);
	
	public String passfind(String username);
	
	public void memberpasswordupdate(String username,String authokey);
	
	public Optional<MemberEntity> findemail(String username);


	public String deletecodesend(String username);
	
	public String deletemember(String username,String authkey);

	public List<findDto> findbynickname(String keyword);

	public String profileimagesave(MultipartFile files,String useremail);
	
	public MemberEntity memberupdate(String email,String nickname,String profileurl);
	
	public String existingprofile(String profileurl);


}
