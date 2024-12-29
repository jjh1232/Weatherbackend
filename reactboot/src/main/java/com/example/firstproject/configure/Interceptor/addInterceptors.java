package com.example.firstproject.configure.Interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.firstproject.Interceptor.LogInterceptor;

//@Configuration
public class addInterceptors implements WebMvcConfigurer{

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// TODO Auto-generated method stub
		registry.addInterceptor(new LogInterceptor())
		.order(1) //interceptor에서실행순서
		.addPathPatterns("/**")
		//.excludePathPatterns("/**") //인터셉터가 실행되지않을 url패턴 배열형식으로이력
		;
	}
	
}
