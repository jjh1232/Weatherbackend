package com.example.firstproject.configure;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Service.JwtService;
import com.example.firstproject.Service.Memberservice.HistoryService;
import com.example.firstproject.configure.auth.Authorizationdfilter;
import com.example.firstproject.configure.auth.authenticationfilter;
import com.example.firstproject.configure.auth.jwtservicetestfilter;
import com.example.firstproject.configure.auth.oauth2failservice;
import com.example.firstproject.configure.auth.oauth2successfilter;
import com.example.firstproject.configure.websocket.ChatErrorHandler;

@Configuration
@EnableWebSecurity
public class securityconfig {

	@Autowired
	private oauth2loginservice loginservice;
	
	@Autowired
	oauth2successfilter success;
	
	@Autowired
	oauth2failservice fail;
	
	@Autowired
	private JwtService jwtservice;

	@Autowired
	private MemberRepository repository;
	
	@Autowired
	private BCryptPasswordEncoder encode;
	
	@Autowired
	private HistoryService historyservice;

	//아래스프링security의 필터체인은 인증인가없이 체인을 진행하지만 예외처리로 자동으로넘어가 결국api가실행안됨
	//따라서 시큐리티필터체인을 생략하기위해서는 웹시큐리티를등록하고 해당주소로 진행시 시큐리티적용을 생략할수있다
	//웹시큐리티는 http시큐리티의 상위에 있고 이것의 ignoring은 필터체인을 걸리진않지만 각종공격에취약해짐
	//httpsecurity에서 permit은 인증처리결과를 무시하는것이지 필터체인의적용은 된다 
	//따라서 websecurity는 보안과 전혀상관없는 로그인과 공개페이지등에만 이용하는게 좋다
	@Bean 
	public WebSecurityCustomizer webSecurityCustomizaer() { //2.7버전이상엔선 웹도해야함
		return (web)->web.ignoring().antMatchers("/open/**");
				//.antMatchers("/**");
				
		//자세한설명링크 
		//https://velog.io/@ksiisk99/SpringSecurity1
		
	}

	//스프링 부트 시큐리트 cors설정
	//스프링 부트 시큐리트 cors설정
	@Bean
    public CorsConfigurationSource corsConfigurationSource() {
		//함수형인터페이스다 까보면 map으로 관련 콘피그를받아서 넘기는 구조
        CorsConfiguration config = new CorsConfiguration();//콘피그설정
        
        config.setAllowCredentials(true);//이게 트루일때 절대 스트링기반 *값을 줄수없다 때문에 list.of로 우회하는것
        //쿠키와 같은 보안요청을 허용할 것인지 이걸사용하면 좀더 보안이 까다로워짐 
        //sse설정시바꿧음잠간
        config.setAllowedOriginPatterns(List.of("http://localhost:3001"));//리스트오브로주네..이거널값은안되는거자늠..
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*")); //얼로우헤더는 preflightrequest의 응답에사용되는헤더 
        //-> 실제요청에 사용할수있는 http헤더의목록나열 ->클라이언트가 사용할수있는 헤더목록 
        config.setExposedHeaders(List.of("Authorization","Refreshtoken","userinfo")); //클라이언트가 응답에 접근할수 있는권하을주는 헤더
       
        //*로모두포함이안되서일일히포함해야함..
        //->응답 받은후 읽기 접근이 허용되는 헤더
        //config.setMaxAge(6000L); -> preflight요청을 클라이언트가 캐싱할수있는시간지정 
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); //소스?
        source.registerCorsConfiguration("/**", config); //레지스터에추가하는듯?
        return source;
    }
	
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
	
		
		
		http.csrf().disable()
			.cors()
			.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.formLogin().disable()
			.httpBasic().disable()
			.authorizeRequests()
			.antMatchers("/admin/**").hasAnyRole("Admin")//설정할거먼저 위로올려야한다
			.antMatchers("/**").permitAll()// 필터체인동작에서 인증/인가예외가발생해도 
			
			//exception필터를 거치지 않고 인증객체 존재여부상관없이 api호출이 이루어지는것
			//지금못쓰는데..이상함
			.anyRequest().authenticated()
			.and()
			.oauth2Login()
			.authorizationEndpoint() //요청url설정
			.baseUri("/oauth2/authorization") //이렇게시작하면 요청인것으로 통일설정
			.and()
			.redirectionEndpoint() //콜백주소설정
			.baseUri("/callback/**") //이걸로시작해야 콜백주소로 간다 시큐리티처리할수있게
									//yml설정에 리다이렉트는또각자
			.and()
			.userInfoEndpoint()
			.userService(loginservice)
			.and()
			.successHandler(success)
			.failureHandler(fail)
			.and()
			.apply(new Customfilter1());
			//.and()
			//.addFilterBefore(new jwtservicetestfilter(jwtservice), UsernamePasswordAuthenticationFilter.class);
			
		
		return http.build();
		
	}
	
	
	//머미리생성해서 매니저주입이안된다는데 잘모루것다
	public class Customfilter1 extends AbstractHttpConfigurer<Customfilter1,HttpSecurity>{

	public void configure(HttpSecurity http) {
		//시큐리티객체에 공유된 Authenticationmanegerbuilder인스턴스를검색
		AuthenticationManager manager=http.getSharedObject(AuthenticationManager.class);
		
		http.addFilter(new authenticationfilter(manager,jwtservice,encode,repository,historyservice));
		http.addFilter(new Authorizationdfilter(manager,repository,jwtservice));
	}
	
	
	}
	
}