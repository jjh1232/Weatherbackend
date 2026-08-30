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
import com.example.firstproject.Service.Memberservice.EmailVerifyService;
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

	//가입 인증 토큰 발급/재발송/검증은 전부 여기가 맡는다
	@Autowired
	private EmailVerifyService emailverifyservice;
	
	
	
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

			if(!"Y".equals(entity.getAuth())) {
				System.out.println("이메일인증을진행하지않은계정입니다");

				/* ★ 예전엔 여기서 구형 방식으로 메일을 보내고
				       entity.setAuth(authkey); repository.save(entity);
				   처럼 인증 코드를 auth 컬럼에 덮어썼다.

				   auth 는 "Y"/"N" 만 담는 로그인 게이트 플래그다(가입 때 "N" 으로 만든다).
				   거기에 코드를 넣으면 두 가지가 한꺼번에 망가진다.
				     1) 그 값은 영원히 "Y" 가 아니므로 그 계정은 다시는 로그인되지 않는다.
				     2) 로그인에 실패할 때마다 값이 또 바뀌고 메일이 또 나간다.
				   인증 토큰은 email_verification 테이블이 따로 관리한다(EmailVerifyService).
				   재발송도 그쪽에 쿨다운·하루한도까지 붙은 resend 가 이미 있으므로 그걸 쓴다.
				   auth 는 링크를 눌렀을 때 EmailVerifyService.verify 가 "Y" 로 바꾼다. */
				emailverifyservice.resend(username);

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
