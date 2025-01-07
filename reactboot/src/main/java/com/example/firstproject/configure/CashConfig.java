package com.example.firstproject.configure;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



import lombok.extern.slf4j.Slf4j;

/*
@Configuration
@EnableCaching //스프링부트캐슁을 사용함을선언
@Slf4j
public class CashConfig {
 // 스프링내장 캐쉬매니저 쓸때
	@Bean
	public CacheManager cacheManager() {
		log.info("캐쉬콘피그시작");
		//메모리상에 캐쉬저장 Concurrentmapcachemanager
		return new ConcurrentMapCacheManager("codeCache");
	}
	
	}
	*/
	
