package com.example.firstproject.Service.Memberservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.firstproject.CustomError.CustomException;
import com.example.firstproject.CustomError.ErrorCode;
import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.Memberform;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.Weather.MemberUpdateDto;
import com.example.firstproject.Dto.Weather.userregionDto;
import com.example.firstproject.Dto.follow.findDto;
import com.example.firstproject.Dto.userdataDto.UserDto;
import com.example.firstproject.Dto.userdataDto.ProfileUpdateDto;
import com.example.firstproject.Dto.userdataDto.UserPageDto;
import com.example.firstproject.tools.ImageExtension;
import com.example.firstproject.Entity.Address;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.follow.FollowEntity;
import com.example.firstproject.Handler.FollowHandler;
import com.example.firstproject.Handler.MemberHandler;
import com.example.firstproject.Handler.NoticeHandler;
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
	
	private final FollowHandler followhandler;

	private final EmailVerifyService emailverifyservice;
	
	private final NoticeHandler noticehandler;
	@Override
	public MemberDto membercreate(Memberform form) {
		// TODO Auto-generated method stub
		
	
		//셀프로해야행
		String newpass=passen.encode(form.getPassword());//시큐리티로그인도 인코딩해줌
		Address regions=new Address();
		
		if(form.getRegion().equals("")) {
			System.out.println("레기온빈값");
			regions=Address.builder().juso("서울특별시  종로구  청운효자동").gridx("60").gridy("127").build();
		}
		else {
			System.out.println("레기온있음");
		 regions=Address.builder().juso(form.getRegion()).gridx(form.getGridx()).gridy(form.getGridy())
		.build();
		}
	
		String profileid=form.getProfileid();
	
		
		MemberEntity entity=MemberEntity.builder()
				
				.username(form.getUsername())
				
				.nickname(form.getNickname())
				.password(newpass)
				
								//auth 컬럼은 이제 "인증 여부" 만 담는다. 토큰은 email_verification 테이블로 갔다.
				.auth("N")
				.provider("mypage")
				.providerid(null)
				.homeaddress(regions)
				.role("ROLE_User")
				.profileid(profileid)
				//동의 시각은 서버 시계로 찍는다. 클라이언트가 보낸 시각은 믿을 수 없다.
				.agreedat(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd/HH:mm:ss")))
				.build();
		
		
		System.out.println(entity.toString());
		MemberEntity saved=handler.membercreate(entity);

		//인증 토큰 발급 + 메일 발송. 토큰 원본은 메일에만 실리고 DB 에는 해시만 남는다.
		emailverifyservice.issue(saved);
		//멤버정보리턴
		MemberDto dto=entity.toDto(entity.getId(),entity.getUsername(),
				entity.getProfileid(),entity.getNickname(),
				entity.getRole(), entity.getProvider(),
				entity.getProviderid(),entity.getHomeaddress(),
				entity.getRegdate(), entity.getUpdatered());
		
		
		
		
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
	public boolean Emailauth(String username) {
		// TODO Auto-generated method stub
		boolean check=handler.emailcheck(username);
		if(check) {
			System.out.println("이미존재하는이메일");
			String a="가입불가";
			
		}
		else {
			System.out.println("가입가능한이메일");
			String auth="가입가능";
			
		}
		return check;
	
	}

	//[삭제됨] memberlogin(email,password)
	///open/memberlogin 전용이었고 해당 API와 함께 제거. 로그인은 Spring Security 가 담당한다.

	@Override
	public Map<String,String> passfind(String username) {
		// TODO Auto-generated method stub
		//에러코드
		MemberEntity entity=handler.findemail(username).orElseThrow(()->{
			return new CustomException(HttpStatus.NOT_FOUND,ErrorCode.NOT_FOUND_USER);
		});
		//데이터담기
		Map<String,String> data=new HashMap<>();
	
		if(!entity.getProvider().equals("mypage")) {
			//타사이트로그인 메일안보낸다
			data.put("status","oauthuser");
			data.put("provider",entity.getProvider());
			data.put("username", entity.getUsername());
			return data;
		}
		
		//메일서비스 
		EmailMessage mail=EmailMessage.builder()
				.to(entity.getUsername())
				.subject("임시비밀번호발급")
				.build();
		String authkey=mailservice.sendmail(mail, "passfind");
		handler.passwordupdate(username, authkey);
		//내사이트 정보
				data.put("status","Success");
				data.put("provider",entity.getProvider());
				data.put("username", entity.getUsername());
		return data;
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
		MemberEntity entity=handler.findemail(username).get();
		log.info("유저네임:"+username);
		log.info("인증키:"+authkey);
		String authkeyconfirm=deleterepo.getdeletecode(username);
		log.info("저장소확인:"+authkeyconfirm);
		
		//일단 팔로우 연관관계 삭제
		List<FollowEntity> followlist=followhandler.findbytofrom(entity.getId());
		
		followlist.stream().forEach(System.out::println);
		
		if(authkey.equals(authkeyconfirm)) {
			log.info("인증키가같습니다!");
			for (FollowEntity fols:followlist) {
				fols.setFrommember(null);
				fols.setTomember(null);
			}
			
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
		//더 이상 쓰지 않는다. 이메일 인증은 EmailVerifyService.verify(token) 이 담당한다.
		//예전 방식은 (1) 토큰을 member.auth 에 넣어 상태와 겸용, (2) 만료 없음,
		//(3) BCrypt 해시를 URL 에 그대로 노출, (4) 없는 이메일이면 500 이었다.
		throw new UnsupportedOperationException("EmailVerifyService.verify(token) 를 사용할 것");
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




	//업로드 루트(application.yml: app.upload.public-dir)
	@Value("${app.upload.public-dir}")
	private String uploadroot;

	/* 이미지 한 장을 subfolder 에 저장하고 "/파일명" 을 돌려준다.

	   파일명에 이메일을 붙이지 않는다. public 폴더라 URL 이 그대로 노출되는데
	   예전 방식(/uuid_이메일)은 가입 이메일이 주소창에 찍혔다.
	   UUID 만 쓰면 "/" + 36 + 확장자 = 41자라 varchar(45) 컬럼에도 그대로 들어간다. */
	@Override
	public String imagesave(MultipartFile file,String subfolder) {
		if(file==null||file.isEmpty()) {
			return null;
		}
		File savefolder=new File(uploadroot+File.separator+subfolder);
		if(!savefolder.exists()) {
			savefolder.mkdirs();
		}

		// 업로더가 보낸 확장자를 그대로 쓰면 x.html 이 그대로 저장되고
		// /userprofileimg/x.html 이 text/html 로 서빙된다(저장형 XSS).
		// 허용 목록에 없으면 400 으로 끊는다.
		String ext=ImageExtension.resolve(file);

		String savename=UUID.randomUUID().toString()+ext;
		Path savepath=Paths.get(savefolder.getAbsolutePath(),savename);
		try {
			file.transferTo(savepath);
		} catch (IllegalStateException | IOException e) {
			log.info("이미지저장실패:"+savepath);
			e.printStackTrace();
			return null;
		}
		return "/"+savename;
	}

	//교체/삭제된 옛 파일 정리
	@Override
	public void imagedelete(String subfolder,String url) {
		if(url==null||url.isBlank()) {
			return;
		}
		Path target=Paths.get(uploadroot+File.separator+subfolder+url.replace("/",File.separator));
		try {
			if(Files.exists(target)) {
				Files.delete(target);
				log.info("이미지삭제:"+target);
			}
		} catch (IOException e) {
			log.info("이미지삭제실패:"+target);
		}
	}

	/* 닉네임/소개/프로필/배경만 바꾼다.
	   memberupdate 와 달리 setHomeaddress 를 부르지 않는다(지역 보존). */
	@Override
	public MemberEntity profileupdate(String email,ProfileUpdateDto dto,String profileurl,String backgroundurl) {
		MemberEntity member=handler.findemail(email).orElseThrow(()->{
			return new IllegalArgumentException("이메일이존재하지않아수정실패");
		});

		member.setNickname(dto.getName());
		member.setMyintro(dto.getMyintro());

		//올라온 파일이 있을 때만 교체한다. 안 보내면 기존 이미지를 유지.
		if(profileurl!=null) {
			member.setProfileimg(profileurl);
		}
		if(backgroundurl!=null) {
			member.setProfilebackground(backgroundurl);
		}

		if(member.getRole().equals("ROLE_TEMP")) {
			member.setRole("ROLE_User");
		}
		//트랜잭션 더티체킹으로 반영된다
		return member;
	}

	@Override
	public String profileimagesave(MultipartFile profileimage,String useremail) {
		// TODO Auto-generated method stub
		
		
		File savefolder=new File(uploadroot+File.separator+"userprofileimg");
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
		if(member.getRole().equals("ROLE_TEMP")) {
			member.setRole("ROLE_User");
		}
		//트랜잭션사용시 리턴될떄 자동 수정(더티체킹)
		return member;
	}




	@Override
	public String existingprofile(String profileurl) {
		// TODO Auto-generated method stub
		log.info("삭제url"+profileurl);
		//예전엔 여기만 옛 프로젝트 경로("D:/study프로그램/react/...")가 박혀 있어서
		//저장 경로와 달랐다. 파일을 못 찾으니 조용히 넘어가고 옛 이미지가 계속 쌓였다.
		String filedirectory=uploadroot+"/userprofileimg"+profileurl;
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



	//======================유저페이지데이터=======================================
	@Override
	public Map<String,Object> userpagedate(String username,int page) {
/*
		// TODO Auto-generated method stub
		MemberEntity user=handler.findprofileid(username).orElseThrow(()->{
			return new IllegalArgumentException("해당유저가존재하지 않습니다!");
		});
		UserDto userdto=UserDto.builder()
						.username(user.getUsername())
						.nickname(user.getNickname())
						.profileimg(user.getProfileimg())
						.myintro(user.getMyintro())
						.regdate(user.getRegdate())
						.build();
		
		Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"id"));
		Page<NoticeEntity> notice=noticehandler.findbyidall(user.getId(), pageable);
		
		Page<NoticeDto> noticedto=notice.map((m)->{
			System.out.println("데이터확인"+m.getNoticeuser());
			return new NoticeDto(m);
		
	
					});
		
			
			Map<String,Object> dto=new HashMap<>();
			
			dto.put("user", userdto);
			dto.put("notice", noticedto);
		return dto;
			*/
		return null;
	}

	//유저페이지유저정보
	@Override
	public UserPageDto userprofileuserdata(Long loginid, String profileid) {
		// TODO Auto-generated method stub
		UserPageDto user=handler.findprofileid(profileid,loginid).orElseThrow(()->{
			return new IllegalArgumentException("해당유저가존재하지 않습니다!");
		});
		return user;
	}

	

	@Override
	public MemberEntity findbyid(Long userid) {
		// TODO Auto-generated method stub
		MemberEntity member=handler.findbyid(userid).orElseThrow(()->new IllegalAccessError());
		return member;
	}



	//프로필아이디체크
	@Override
	public boolean profileidcheck(String profileid) {
		// TODO Auto-generated method stub
		
		
		return handler.existsByProfileId(profileid);
	}




	@Override
	public Map<String, String> Usernamefind(String username) {
		// TODO Auto-generated method stub
		MemberEntity member=handler.findbyusername(username).orElseThrow(()->
		new CustomException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_USER));
		
		String usernames=member.getUsername();
		String provider=member.getProvider();
		
		Map<String, String> data=new HashMap<>();
		data.put("username", usernames);
		data.put("provider", provider);
		return data;
	}







   
	
	}

