package com.example.firstproject.configure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

import com.example.firstproject.Entity.Address;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.configure.auth.provider.Googleprovider;
import com.example.firstproject.configure.auth.provider.Naverprovider;
import com.example.firstproject.configure.auth.provider.Provider;


import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class oauth2loginservice extends DefaultOAuth2UserService{

	@Autowired
	private MemberRepository repository;
	
	@Autowired
	BCryptPasswordEncoder encode;
	
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// TODO Auto-generated method stub
		System.out.println("오아스로그인서비스");
		String provide=userRequest.getClientRegistration().getRegistrationId();
		
		OAuth2User user=super.loadUser(userRequest);
		
		Provider oauth2user=null;
		
		System.out.println(userRequest.getClientRegistration().getRegistrationId());
		System.out.println(super.loadUser(userRequest).toString());
		
		if(provide.equals("google")) {
			oauth2user=new Googleprovider(user.getAttributes());
			//어트라뷰트 받아올자료를 로드유저로 확인하고 형식으로만들자
			System.out.println("구글로그인"+oauth2user.getusername());
			System.out.println("구글로그인네임;"+oauth2user.getname());
		}
		else if(provide.equals("naver")) {
			System.out.println("네이버");
			log.info("유저정보"+user.getAttributes().get("response"));
			oauth2user=new Naverprovider((Map<String, Object>) user.getAttributes().get("response"));
		
			System.out.println(oauth2user.provider());
		}
		else {
			System.out.println("지원하지않는방식의플랫폼입니다");
			
		}
		
		String username=oauth2user.getusername();
		
		//소셜이 준 이메일을 로그에 남기지 않는다.
		Optional<MemberEntity> opuser=repository.findByUsername(username);
		MemberEntity entity=opuser.orElse(null);
		if(entity==null) {
			Map<String,Object> attrs=new HashMap<>(user.getAttributes());
			//임시객체생성
			
		
			
			System.out.println("새로가입하니다");
			String name=oauth2user.getname();
			String nickname=oauth2user.getnickname();
			//소셜 가입자는 이 비밀번호로 로그인하지 않는다(PrincipalService에서 폼로그인을 막는다).
			//그래도 계정마다 다른 값을 넣어둬야, 혹시 폼로그인 차단이 뚫리더라도
			//하나가 털렸을때 나머지 계정까지 같이 열리는 일이 없다.
			String password=encode.encode(UUID.randomUUID().toString());
			String role="ROLE_TEMP";
			String provider=oauth2user.provider();
			String providerid=oauth2user.prividerid();
			String auth="Y";
			//프로필아이디생성
			String base=oauth2user.getname().split("@")[0];
			String profileid=base;
			int suffix=1;
			while(repository.existsByProfileid(profileid)) {
				profileid=base+suffix;
				suffix++;
				
			}
			//디폴트값이안들어가서직접
			Address address=Address.builder().juso("서울특별시  종로구  청운효자동").gridx("60").gridy("127")
					.build();
			entity=MemberEntity.builder()
					.username(username)
					
					.nickname(nickname)
					.password(password)
					.role(role)
					.auth(auth)
					.homeaddress(address)
					.provider(provider)
					.providerid(providerid)
					.profileid(profileid)
					.build();
			
			repository.save(entity);
		
		}else {
			System.out.println("기존에아이디가존재하는회원입니다"); 
			if(entity.getProvider().equals("mypage")) {
				log.info("사이트로그인아이디입니다!");
				throw new OAuth2AuthenticationException("asd");
			}
			
			else {
			//위에추가한throw로 선언한 에러를 지정해야함!
				log.info("타사로그인서비스");
			
			}
		}
		//소셜이 준 속성 전체에는 이메일·프로필사진 URL·고유ID 가 들어 있다.
		//디버깅 때 유용했지만 로그에 개인정보를 그대로 쌓게 되므로 키 이름만 남긴다.
		log.info("소셜 속성 수신: keys={}", user.getAttributes().keySet());
		return new PrincipalDetails(entity,user.getAttributes());
		
	}
}
