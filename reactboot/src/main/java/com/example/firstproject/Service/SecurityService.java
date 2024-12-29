package com.example.firstproject.Service;

import java.security.Key;
import java.security.Signature;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class SecurityService {
//실제론이러면안되지만연습용 시크릿키생성인듯
	private static final String SECRET_KEY="asdkfekssdfdsfdskjfklsdfklsdjfksldjfsdlkfjsdkldf";
	
	//로그인서비스 던질때같이씀
	public String createToken(String subject,long expTime) {
		if(expTime<=0) {
			throw new RuntimeException("만료시간이 0보다커야함");
		}
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;//알고리즘 생성인듯 서브
		byte[] secretKeyBytes=DatatypeConverter.parseBase64Binary(SECRET_KEY);//밸류값 hs256임으로256비트이상크기
		//자바시크리키
		Key signingkey=new SecretKeySpec(secretKeyBytes,signatureAlgorithm.getJcaName());
		 return Jwts.builder()   //자바8에생긴빌더로토큰생성
				 .setSubject(subject)//내용셋
				 .signWith(signingkey,signatureAlgorithm)//변조된키와 알고리즘
				 .setExpiration(new Date(System.currentTimeMillis()+expTime)) //만료시간설정
				 .compact();//끝
		 		
	}
	//토큰검증하는 메서드를boolean~
	public String getsubject(String token) {
		//서브젝트꺼내오기
		Claims claims=Jwts.parserBuilder()//디코딩빌더인듯? 
				.setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))//컨버터 시크릿키로 콘버터이용
				.build()//빌드
				.parseClaimsJws(token)//토큰값 변형
				.getBody(); //바디얻기?
		return claims.getSubject(); //얻은 내용출력
	}
}
