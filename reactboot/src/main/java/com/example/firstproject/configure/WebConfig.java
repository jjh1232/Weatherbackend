package com.example.firstproject.configure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer{

	//시큐리티가아닌 스프링 설정 이거설정안하면 날씨api가못받아오는경우가발생
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") //CORS를 적용할 URL패턴을 정의한다 
				.allowedOriginPatterns("http://localhost:3001/") //자원공유를 허락할 ORIGIN지정!sse에할떄수정했음참고
				.allowedMethods("*")  //허용할 Httpmethod
				.allowedHeaders("*") //헤더지정 기본은 content-type,accept및origin같은간단ㅇ한요청헤더만허용
				.exposedHeaders("*")  //클라이언트가 사용할수있는 노출헤더설정
				.allowCredentials(true) //쿠키나인증헤더 를지정 와일드카드사용불가 오리진에
				.maxAge(7200);// (2시간)  원하는 시간만큼 pre-flight리퀘스트를캐싱
	}		
}
