package com.example.firstproject.Service;

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
	
	//@value()는 빈주입전이라안된다함
	private String secretkey= "${sercret.jwtkey}";
	
	
	//토큰생성 이건 com0 라이브러리를 사용했음 
	//jsonwebtoken 라이브러리는 Jwts.builder.setsubject.signwih.compact()로생성한다
	//토큰을풀때도 Jwts.parserBuilder 로 사용 
	public String createtoken(PrincipalDetails member) {
		return JWT.create()
				.withSubject("Accesstoken")
				.withClaim("username", member.getUsername())
				
				.withClaim("nickname", member.getMember().getNickname())
				.withClaim("role", member.getMember().getRole())
				.withExpiresAt(new Date(System.currentTimeMillis()+(1000*60)))//일단*60뺴고
				.sign(Algorithm.HMAC512(secretkey));
			
	}
	
	//리프레쉬토큰생성
	public String createrefreshtoken() {
		
		return JWT.create()
			.withSubject("refreshtoken")
			.withExpiresAt(new Date(System.currentTimeMillis()+(1000*60*60*24)))
			.sign(Algorithm.HMAC512(secretkey));

	}

	//db에 리프레쉬토큰저장
	@Transactional
	public void Setrefreshtoken(String username, String represhtoken) {
		// TODO Auto-generated method stub
		Optional<MemberEntity> member=memberrepository.findByUsername(username);
		MemberEntity nwemem=member.get();
		//변경감지가왜..
		nwemem.setRefreshtoken(represhtoken);
		//memberrepository.save(nwemem);
		
		
	}
	//리프레쉬토큰 찾기
	@Transactional //이거 영속성엔티티문제해결보기
	public Optional<MemberEntity> findbyrefreshtoken(String refreshtoken) {
		Optional<MemberEntity> member=memberrepository.findByrefreshtoken(refreshtoken);
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