package com.example.firstproject.Service.Memberservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.Memberform;
import com.example.firstproject.Dto.Weather.MemberUpdateDto;
import com.example.firstproject.Dto.Weather.userregionDto;
import com.example.firstproject.Dto.follow.findDto;
import com.example.firstproject.Entity.Address;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Handler.MemberHandler;
import com.example.firstproject.Handler.WeatherServiceHandler;
import com.example.firstproject.Repository.EmitterRepository;
import com.example.firstproject.Repository.Memberdeleterepository;
import com.example.firstproject.Repository.NoticeRepository;
import com.example.firstproject.Service.mailservice.mailsandservice;
import com.example.firstproject.Vo.EmailMessage;
import com.example.firstproject.controller.SSEController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberServiceImpl implements MemberService{

	
	
	@Autowired
	private MemberHandler handler;
	
	@Autowired
	private mailsandservice mailservice;
	
	@Autowired
	private BCryptPasswordEncoder passen;
	
	private final WeatherServiceHandler weather;
	
	private final Memberdeleterepository deleterepo;
	
	@Override
	public MemberDto membercreate(Memberform form) {
		// TODO Auto-generated method stub
		
	
		
		String newpass=passen.encode(form.getPassword());
		Address regions=Address.builder().juso(form.getRegion()).gridx(form.getGridx()).gridy(form.getGridy())
		.build();
		
		
		MemberEntity entity=MemberEntity.builder()
				
				.username(form.getUsername())
				
				.nickname(form.getNickname())
				.password(newpass)
				
				.provider("mypage")
				.providerid(null)
				.homeaddress(regions)
				.role("ROLE_User")
				
				.build();
		
		
		System.out.println(entity.toString());
		handler.membercreate(entity);
		
		EmailMessage message=EmailMessage.builder()
				.to(form.getUsername())
				.subject("이메일인증메일")
				.build();
		System.out.println("이메일인증시작");
		String authkey=mailservice.sendmail(message,"email");
		entity.setAuth(authkey);
		MemberDto dto=entity.toDto(entity.getId(),entity.getUsername(), entity.getPassword(),
				entity.getNickname(),
				entity.getRole(), entity.getRefreshtoken(), entity.getProvider(),
				entity.getProviderid(),entity.getHomeaddress(),
				entity.getRed(), entity.getUpdatered());
		
		
		
		
		return dto;
	}

	
	
	
	@Override
	public long findbyemail(String username) {
		// TODO Auto-generated method stub
			Optional<MemberEntity> opentity=handler.findemail(username);
			if(opentity.isPresent()) {
				MemberEntity entity=opentity.get();
				
				return entity.getId();
			}
			else{
				System.out.println("이메일을찾지못함");
				return 0;
			}
	
	}

	@Override
	public String Emailauth(String username) {
		// TODO Auto-generated method stub
		Long check=handler.emailcheck(username);
		if(check>=1) {
			System.out.println("이미존재하는이메일");
			String a="가입불가";
			return a;
		}
		else {
			System.out.println("가입가능한이메일");
			String auth="가입가능";
			return auth;
		}
	
	}

	@Override
	public Object memberlogin(String email, String password) {
		// TODO Auto-generated method stub
		Optional<MemberEntity> opentity=handler.login(email);
		if(opentity.isPresent()) {
			MemberEntity entity=opentity.get();
			if(password.equals(entity.getPassword())) {
				System.out.println("비번맞음");
				return entity;
			}else {
				System.out.println("비번틀림");
				return "x";
			}
			
			
		}else {
			return null;
		}
		
	}

	@Override
	public String passfind(String username) {
		// TODO Auto-generated method stub
		Optional<MemberEntity> op=handler.findemail(username);
		MemberEntity entity=op.get();
		//메일서비스 일단보류 
		EmailMessage mail=EmailMessage.builder()
				.to(entity.getUsername())
				.subject("임시비밀번호발급")
				.build();
		String authkey=mailservice.sendmail(mail, "passfind");
		
		return authkey;
	}

	@Override
	public void memberpasswordupdate(String username, String authokey) {
		// TODO Auto-generated method stub
		System.out.println("임시비밀번호발급"+authokey);
		handler.passwordupdate(username, authokey);
	}

	@Override
	public Optional<MemberEntity> findemail(String username) {
		// TODO Auto-generated method stub
		Optional<MemberEntity> opentity=handler.findemail(username);
		
		return opentity;
	}

	@Override
	public String deletecodesend(String username) {
		// TODO Auto-generated method stub
		log.info(username+"님 delete이메일발송!");
		EmailMessage deletemail=EmailMessage.builder().to(username).subject("삭제코드발급").build();
		String authkey=mailservice.sendmail(deletemail, "deletemail");
		log.info("인증키:"+authkey);
		deleterepo.memberdeletecodesave(username, authkey);
		return authkey;
	}

	@Override
	public String deletemember(String username,String authkey) {
		// TODO Auto-generated method stub
		log.info("유저네임:"+username);
		log.info("인증키:"+authkey);
		String authkeyconfirm=deleterepo.getdeletecode(username);
		log.info("저장소확인:"+authkeyconfirm);
		if(authkey.equals(authkeyconfirm)) {
			log.info("인증키가같습니다!");
			
			MemberEntity entity=handler.findemail(username).get();
			handler.deletemember(entity);
		}
		else {
			log.info("인증키가다릅니다 ㅜ!");
		}
		
		return null;
	}


	//회원이메일인증====================================================
	@Override
	public int auth(String username, String authokey) {
		// TODO Auto-generated method stub
		Optional<MemberEntity> opentity=handler.findemail(username);
		MemberEntity entity=opentity.get();
		
		if(entity.getAuth().equals(authokey)){
			System.out.println("인증키가 맞습니다");
			entity.setAuth("Y");
			handler.membercreate(entity);
			
			return 0;
			
		}else {
			System.out.println("인증키가 다릅니다인증실패!");
			
			return 1;
		}
		
	}




	@Override
	public List<findDto> findbynickname(String keyword) {
		// TODO Auto-generated method stub
		log.info("닉네임으로 멤버찾기");
		List<MemberEntity> list=handler.findbynickname(keyword);
		List<findDto> dtolist=new ArrayList<>();
		for(MemberEntity entity:list) {
			findDto dto=findDto.builder()
					.username(entity.getUsername())
					.nickname(entity.getNickname())
					.build();
			dtolist.add(dto);
			
		}
		return dtolist;
	}




	@Override
	public String profileimagesave(MultipartFile profileimage,String useremail) {
		// TODO Auto-generated method stub
		
		
		File savefolder=new File("D:/study프로그램/react/bootproject/public/userprofileimg");
		if(savefolder.exists()==false) {//폴더가 있으면 트루없으면폴스
			savefolder.mkdirs();
			
		}
		String uuid=UUID.randomUUID().toString();
		String savefilename=savefolder.toPath()+File.separator+uuid+"_"+useremail;
		log.info("궁금해서topath내용:"+savefolder.toPath());
		Path savePath=Paths.get(savefilename);
		try {
			profileimage.transferTo(savePath);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			log.info("경로오류");
			e.printStackTrace();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.info("경로오류");
			e.printStackTrace();
			
		}
				
	
				
		return "/"+uuid+"_"+useremail;
	
	}




	@Override
	public MemberEntity memberupdate(String email,MemberUpdateDto dto, String profileurl) {
		// TODO Auto-generated method stub
		Address regions=Address.builder().juso(dto.getRegion()).gridx(dto.getGridx()).gridy(dto.getGridy())
				.build();
		MemberEntity member=handler.findemail(email).orElseThrow(()->{
			return new IllegalArgumentException("이메일이존재하지않아수정실패");
		});
		if(profileurl !=null) {
			member.setNickname(dto.name);
			member.setProfileimg(profileurl);
			member.setHomeaddress(regions);
			//닉네임과프로필변경
		}else {
			member.setNickname(dto.name);
			member.setHomeaddress(regions);
			//닉네임만변경
		}
		//트랜잭션사용시 리턴될떄 자동 수정(더티체킹)
		return member;
	}




	@Override
	public String existingprofile(String profileurl) {
		// TODO Auto-generated method stub
		log.info("삭제url"+profileurl);
		String filedirectory="D:/study프로그램/react/bootproject/public/userprofileimg"+profileurl;
		String path=filedirectory.replace("/",File.separator);
		log.info("삭제패스"+path);
		Path deleteprofilepath=Paths.get(path);
		log.info("파일여부:"+Files.exists(deleteprofilepath));
		if(Files.exists(deleteprofilepath)) {
			try {
				
				Files.delete(deleteprofilepath);
				log.info("삭제성공");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("삭제오류");
				e.printStackTrace();
			}
		}
		return null;
	}


   
	
	}

