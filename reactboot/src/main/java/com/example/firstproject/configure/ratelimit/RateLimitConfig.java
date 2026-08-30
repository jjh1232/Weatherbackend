package com.example.firstproject.configure.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 요청 제한 인터셉터 등록.
 * 로그인 없이 부를 수 있는 조회/가입 API 에만 건다. 나머지는 토큰으로 이미 걸러진다.
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

	@Bean
	public RateLimitInterceptor ratelimitInterceptor() {
		//카운터를 들고 있어야 하므로 반드시 하나만 만든다(빈으로 등록).
		return new RateLimitInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(ratelimitInterceptor())
				.order(0) //다른 인터셉터보다 먼저. 막을 요청이면 뒷일을 안 하는 게 맞다.
				.addPathPatterns(
						"/open/emailcheck",
						"/open/profileidcheck",
						"/open/usernamefind/**",
						"/open/passwordfind",
						"/open/member/resend",
						"/open/membercreate");
	}
}
