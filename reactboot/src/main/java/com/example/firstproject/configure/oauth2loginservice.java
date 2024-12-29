package com.example.firstproject.configure;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

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
		
		System.out.println("유저:"+username);
		Optional<MemberEntity> opuser=repository.findByUsername(username);
		MemberEntity entity=opuser.orElse(null);
		if(entity==null) {
			System.out.println("새로가입하니다");
			String name=oauth2user.getname();
			String nickname=oauth2user.getnickname();
			String password=encode.encode("asdasdwekjer234325325");
			String role="ROLE_User";
			String provider=oauth2user.provider();
			String providerid=oauth2user.prividerid();
			String auth="Y";
			entity=MemberEntity.builder()
					.username(username)
					
					.nickname(name)
					.password(password)
					.role(role)
					.auth(auth)
					.provider(provider)
					.providerid(providerid)
					.build();
			System.out.println("유저엔티티작성완료"+entity);
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
		System.out.println("네이버도잘나오나어트리뷰트스"+user.getAttributes());
		return new PrincipalDetails(entity,user.getAttributes());
		
	}
}
