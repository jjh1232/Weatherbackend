package com.example.firstproject.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.configure.PrincipalDetails;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;


@Service
public class JwtService {

	@Autowired
	private MemberRepository memberrepository;
	
	//@Value 는 이 클래스가 스프링 빈(@Service)이고 어디서도 new 로 만들지 않으므로 정상 동작한다.
	//@Value 가 안 먹는 경우는 static 필드이거나 new 로 직접 생성한 객체일 때다.
	//값은 application.yml 의 jwt.secret -> application-secret.yml 또는 환경변수 JWT_SECRET
	@Value("${jwt.secret}")
	private String secretkey;
	
	
	//토큰생성 이건 com0 라이브러리를 사용했음 
	//jsonwebtoken 라이브러리는 Jwts.builder.setsubject.signwih.compact()로생성한다
	//토큰을풀때도 Jwts.parserBuilder 로 사용 
	public String createtoken(PrincipalDetails member) {
		return JWT.create()
				.withSubject("Accesstoken")
				.withClaim("id", member.getMember().getId())
				.withClaim("username", member.getUsername())
				.withClaim("profileid", member.getMember().getProfileid())
				.withClaim("nickname", member.getMember().getNickname())
				.withClaim("role", member.getMember().getRole())
				.withClaim("provider", member.getMember().getProvider())
				.withExpiresAt(new Date(System.currentTimeMillis()+(1000*60*60)))//일단*60뺴고
				.sign(Algorithm.HMAC512(secretkey));
			
	}
	
	//리프레쉬토큰생성
	public String createrefreshtoken() {
		
		return JWT.create()
			.withSubject("refreshtoken")
			.withExpiresAt(new Date(System.currentTimeMillis()+(1000*60*60*24)))
			.sign(Algorithm.HMAC512(secretkey));

	}

	//=====================================================================
	// 리프레쉬 토큰은 원문이 아니라 SHA-256 해시로 저장한다.
	// 원문을 그대로 두면 DB 가 유출됐을 때 그 값만으로 남의 세션을 이어받을 수 있다
	// (비밀번호를 해시해서 저장하는 것과 같은 이유).
	// 솔트는 쓰지 않는다 - 토큰 자체가 서명된 임의값이라 사전 대입이 성립하지 않고,
	// 솔트를 쓰면 "토큰 값으로 회원을 찾는" 조회가 불가능해진다.
	//
	// 저장(Setrefreshtoken)과 조회(findbyrefreshtoken) 양쪽에서 같은 해시를 쓰므로
	// 부르는 쪽 코드는 그대로 원문을 넘기면 된다.
	//=====================================================================
	public String hashrefreshtoken(String token) {
		if(token==null) {
			return null;
		}
		try {
			MessageDigest digest=MessageDigest.getInstance("SHA-256");
			byte[] hashed=digest.digest(token.getBytes(StandardCharsets.UTF_8));

			StringBuilder builder=new StringBuilder(hashed.length*2);
			for(byte b:hashed) {
				builder.append(String.format("%02x", b));
			}
			return builder.toString();
		}
		catch (NoSuchAlgorithmException e) {
			//SHA-256 은 모든 JVM 이 반드시 갖고 있어야 하는 알고리즘이라 실제로는 안 난다.
			throw new IllegalStateException("SHA-256 을 사용할 수 없습니다", e);
		}
	}

	//db에 리프레쉬토큰저장
	@Transactional
	public void Setrefreshtoken(String username, String represhtoken) {
		// TODO Auto-generated method stub
		MemberEntity member=memberrepository.findByUsername(username).orElseThrow(()->new RuntimeException("유저가없어요"));
		
		//변경감지가왜..
		member.setRefreshtoken(hashrefreshtoken(represhtoken));
		//memberrepository.save(nwemem);
		
		
	}
	//로그아웃 - 저장해둔 리프레쉬 토큰을 지운다.
	//이걸 안 하면 로그아웃해도 그 토큰으로 /refresh 가 남은 기간(24시간) 내내 통해서,
	//리프레쉬 토큰을 서버에 보관하는 이유(무효화) 자체가 사라진다.
	@Transactional
	public void clearrefreshtoken(String username) {
		MemberEntity member=memberrepository.findByUsername(username).orElseThrow(()->new RuntimeException("유저가없어요"));
		member.setRefreshtoken(null);
	}

	//리프레쉬토큰 찾기
	@Transactional //이거 영속성엔티티문제해결보기
	public Optional<MemberEntity> findbyrefreshtoken(String refreshtoken) {
		//받은 건 원문이므로 저장할 때와 같은 방식으로 해시해서 찾는다
		Optional<MemberEntity> member=memberrepository.findByrefreshtoken(hashrefreshtoken(refreshtoken));
		return member;
	}
	
	//사용자 리프레쉬토큰 시간계산  
	public boolean isneedrefreshtoken(String token) {
		//사용자리프레쉬토큰 유효시간계산
		try {
		Date expires=JWT.require(Algorithm.HMAC512(secretkey))
				.build()
				.verify(token)
				.getExpiresAt();
		
		Date current=new Date(System.currentTimeMillis());
		
		Calendar calenda=Calendar.getInstance();//추상메소드라 겟인스턴스로 생성해야함
		calenda.setTime(current);//현재시간입력
		calenda.add(Calendar.HOUR, 12);//12시간더하기
		//12시간더한거 추가
		Date expiration=calenda.getTime();
		
		//12시간보다 전이면 
		if(expires.before(expiration)) {
			System.out.println("12시간이내에만료 ");
			return true;
		}
		
		}
		catch (TokenExpiredException e) {
			// TODO: handle exception
			return false;
		}
	System.out.println("토큰만료");
	return false;	
	}
	
	//액세스 토큰 재발급 기준(분). 남은 시간이 이보다 적어지면 새로 발급한다.
	public static final int ACCESS_RENEW_MINUTES = 10;

	//액세스 토큰을 새로 발급할 때가 됐는가.
	//예전엔 인가 필터가 요청마다 무조건 재발급했다. 그러면 리프레쉬 토큰까지 매번
	//회전해서, 병렬 요청끼리 DB 에 저장된 값과 브라우저 쿠키 값이 어긋나는 사고가 난다.
	//만료가 가까울 때만 갱신해도 "쓰는 동안 세션이 연장된다"는 성질은 그대로다.
	public boolean isaccessrenewneeded(String token, int minutes) {
		try {
			Date expires=JWT.require(Algorithm.HMAC512(secretkey))
					.build()
					.verify(token)
					.getExpiresAt();

			long left=expires.getTime()-System.currentTimeMillis();
			return left < (long)minutes*60*1000;
		}
		catch (TokenExpiredException e) {
			//이미 만료된 토큰은 여기서 다룰 일이 아니다(호출 전에 checktokenvalid 로 걸러진다)
			return false;
		}
	}

	//토큰기간 검증
	public boolean checktokenvalid(String token) {
		// TODO Auto-generated method stub
		try {
		Date data=JWT.require(Algorithm.HMAC512(secretkey))
		.build()
		.verify(token)
		.getExpiresAt();
		System.out.println("서비스날짜:"+data);
		}
		catch(TokenExpiredException e){
			System.out.println("토큰만료");
			return false;
		}
		return true;
	}

	//토큰 값중 유저네임가져오기 근데이건뭐임
	public String gettokenclaim(String token) {
		// TODO Auto-generated method stub
		return JWT.require(Algorithm.HMAC512(secretkey))
				.build()
				.verify(token)
				.getClaim("username")
				.asString();
				
				
				
							
				
				
	}
	
	
	//헤더에토큰있는지검증 하는김에 bearer도 검증하자 
	public void tokenvalid(String token) throws Exception {
		if(token ==null) {
			System.out.println("토큰이없습니다!");
			throw new Exception("토큰없어!");
		}
		else {
			System.out.println("토큰이 있어!");
			if(token.startsWith("Bearer ")){
				System.out.println("올바른토큰입니다!");
				
				
			}
			else {
				System.out.println("잘못된토큰이에용!");
				throw new Exception("Bearer 토큰이아니여!");
			}
		}
	}
	
}