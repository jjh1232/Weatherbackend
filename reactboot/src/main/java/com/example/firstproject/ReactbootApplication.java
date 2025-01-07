package com.example.firstproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableJpaAuditing
@SpringBootApplication
//@PropertySource(value= {"classpath:oauth2.yml"})//다른yml추가라는데모르겟다 
@EnableScheduling //스케줄러추가 
@EntityScan(basePackages = {"com.example.firstproject.Entity"})
public class ReactbootApplication {

	@Bean
	public BCryptPasswordEncoder passencoder() {
		return new BCryptPasswordEncoder();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(ReactbootApplication.class, args);
	}

}
