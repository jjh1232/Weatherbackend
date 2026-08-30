package com.example.firstproject.configure;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer{

	//업로드 루트(application.yml: app.upload.public-dir)
	@Value("${app.upload.public-dir}")
	private String uploadroot;

	//허용 오리진(application.yml: app.cors.allowed-origins). securityconfig 와 같은 값을 본다.
	@Value("${app.cors.allowed-origins}")
	private String[] allowedorigins;

	//시큐리티가아닌 스프링 설정 이거설정안하면 날씨api가못받아오는경우가발생
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") //CORS를 적용할 URL패턴을 정의한다
				//예전엔 "http://localhost:3001/" 처럼 끝에 슬래시가 붙어 있었다.
				//Origin 헤더에는 슬래시가 없어서 이 설정은 영원히 매치되지 않는 죽은 값이었다.
				.allowedOriginPatterns(allowedorigins) //자원공유를 허락할 ORIGIN지정!sse에할떄수정했음참고
				.allowedMethods("*")  //허용할 Httpmethod
				.allowedHeaders("*") //헤더지정 기본은 content-type,accept및origin같은간단ㅇ한요청헤더만허용
				//"*" 는 쓰지 않는다. allowCredentials(true) 인 요청에서는 브라우저가
				//Access-Control-Expose-Headers: * 를 무시한다(자격증명 요청에는 와일드카드 불가).
				//지금 로컬에서 동작하는 건 시큐리티 쪽 CORS 설정이 먼저 헤더를 붙여주기 때문이고,
				//이쪽 설정이 덮어쓰면 프론트가 Authorization/userinfo 를 못 읽는다.
				//securityconfig.corsConfigurationSource() 와 같은 목록을 명시한다.
				.exposedHeaders("Authorization","Refreshtoken","userinfo")
				.allowCredentials(true) //쿠키나인증헤더 를지정 와일드카드사용불가 오리진에
				.maxAge(7200);// (2시간)  원하는 시간만큼 pre-flight리퀘스트를캐싱
	}

	//=====================================================================
	// 업로드 이미지 정적 서빙.
	//
	// 지금까지는 업로드 파일을 리액트 public/ 에 직접 떨어뜨리고
	// 프론트 개발서버가 그대로 서빙해 줘서 이 설정이 없어도 보였다.
	// 배포하면 프론트는 정적 호스팅(읽기 전용)이고 서버는 다른 머신이라
	// 그 방법이 통하지 않는다. 서버가 직접 내보내야 한다.
	//
	// DB 에 저장된 경로가 "/noticeimages/2024/12/30/uuid.png" 형태라
	// 그 경로를 그대로 쓸 수 있도록 폴더명을 URL 에 그대로 노출한다.
	// 프론트는 앞에 API 주소만 붙이면 된다.
	//=====================================================================
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		//"D:/..." 든 "/opt/weave/uploads" 든 file: URI 로 정규화한다.
		//끝에 슬래시가 없으면 마지막 폴더명이 잘려 엉뚱한 곳을 가리킨다.
		String base = Paths.get(uploadroot).toAbsolutePath().toUri().toString();
		if (!base.endsWith("/")) base += "/";

		registry.addResourceHandler("/noticeimages/**")
				.addResourceLocations(base + "noticeimages/");
		registry.addResourceHandler("/userprofileimg/**")
				.addResourceLocations(base + "userprofileimg/");
		registry.addResourceHandler("/userbackgroundimg/**")
				.addResourceLocations(base + "userbackgroundimg/");
	}
}
