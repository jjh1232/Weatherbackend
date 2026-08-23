package com.example.firstproject.configure;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.firstproject.CustomError.CustomException;
import com.example.firstproject.CustomError.ErrorCode;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Service.Memberservice.MemberService;
import com.example.firstproject.Service.mailservice.mailsandservice;
import com.example.firstproject.Vo.EmailMessage;

import ch.qos.logback.core.status.Status;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrincipalService implements UserDetailsService{

	@Autowired
	private BCryptPasswordEncoder enc;
	@Autowired
	private MemberRepository repository;
	
	@Autowired
	private mailsandservice mailservice;
	
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		Optional<MemberEntity> open=repository.findByUsername(username);
		
		if(open.isPresent()) {
			System.out.println("유저잇음");
			MemberEntity entity=open.get();

			//소셜(구글/네이버) 가입 계정은 아이디/비밀번호 로그인을 허용하지 않는다.
			//소셜 가입시에는 쓰지않는 비밀번호가 들어가므로, 여기서 막지않으면
			//그 값을 아는 사람이 소셜계정 아무거나로 로그인할수있게된다.
			//일반가입은 provider가 "mypage", 소셜은 "Google"/"Naver" 로 저장된다.
			String provider=entity.getProvider();
			if(provider!=null && !provider.equals("mypage")) {
				System.out.println("소셜가입계정의 폼로그인 시도 차단: "+username+" / provider:"+provider);
				throw new InternalAuthenticationServiceException(
						provider+" 간편로그인으로 가입한 계정입니다. "+provider+" 로그인을 이용해주세요.");
			}

			if(!entity.getAuth().equals("Y")) {
				System.out.println("이메일인증을진행하지않은계정입니다");
				System.out.println("auth:"+entity.getAuth());
				//여기에러는 저걸로처리되서 넘겻음 
				EmailMessage message=EmailMessage.builder()
						.to(username)
						.subject("이메일인증메일")
						.build();
				System.out.println("이메일인증시작");
				String authkey=mailservice.sendmail(message,"email");
				entity.setAuth(authkey);
				repository.save(entity);
				
				throw new InternalAuthenticationServiceException("이메일인증메일다시보냈습니다 인증후 이용해주세요!");
			}
			System.out.println("인증성공시");
			//이거만들떄 비밀번호를 입력받은값이랑 비교하는듯
			return new PrincipalDetails(entity);
		}

		System.out.println("유저없음");
		//에러마다 메세지그냥정함
		throw new InternalAuthenticationServiceException(username+"은존재하지 않는 계정입니다!");
	}

}
