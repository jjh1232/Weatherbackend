package com.example.firstproject.configure;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class Redisconfig {

	@Value("${spring.data.redis.host}")
	private String host;
	
	@Value("${spring.data.redis.port}")
	private int port;

	// 비번을 안 건 로컬 redis 도 그대로 돌아가야 하므로 기본값을 빈 문자열로 둔다.
	// (${VAR:} 는 "없으면 빈 값" 이라는 뜻. 시크릿이지만 로컬 호환 때문에 예외로 기본값을 준다)
	@Value("${spring.data.redis.password:}")
	private String password;
	
	
	//redis연결을위한 커넥션생성
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		//일단기본으로도되는ㄷ스?
		
		 // Single 모드
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();

        // Cluster 모드
        // RedisClusterConfiguration conf = new RedisClusterConfiguration();

        conf.setHostName(host);
        conf.setPort(port);
		// 비번이 설정돼 있을 때만 넘긴다. 빈 문자열을 그대로 넘기면
		// 비번을 안 건 서버가 "AUTH 를 왜 보내냐" 며 거절한다.
		if (password != null && !password.isBlank()) {
			conf.setPassword(password);
		}

		// 위에서 만든 conf 를 실제로 넘긴다.
		// 예전에는 conf 를 만들어놓고 (host, port) 를 따로 넘겨서 conf 가 통째로 버려졌다.
		// 그래서 setPassword 를 추가해도 아무 효과가 없었다.
		return new LettuceConnectionFactory(conf);
	}
	
	
	@Bean   //레디스config에서만든 팩토리를 매게변수로가져옴
	public CacheManager redisCachemanager(RedisConnectionFactory redisconnnectFactiory) {
		RedisCacheConfiguration conf=RedisCacheConfiguration.defaultCacheConfig()
		.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
		.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
		.entryTtl(Duration.ofMinutes(59))//ttl설정 스케쥴러도있지만 혹시모르니
		;
		
		return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisconnnectFactiory)
				.cacheDefaults(conf).build();
	}

	@Bean
	//이거 Dto도넣기위한 기본 형 근데 Long같은거넣으면 파싱이 되게귀찮다
	public RedisTemplate<String, Object> ObjectredisTemplate(RedisConnectionFactory factory){
		RedisTemplate<String,Object> template=new RedisTemplate<>();
		template.setConnectionFactory(factory);
		//키직렬화 문자열이라 String으로 
		template.setKeySerializer(new StringRedisSerializer()); 
		//밸류직렬화 json형태로 객체를저장함
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		//redis hash필드값직렬화 해쉬도보통String이기떄문
		template.setHashKeySerializer(new StringRedisSerializer());
		//hash의필드값도 다양한객체타입일수있음
		template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
		
		return template;
		
	}
	
	@Bean
	public RedisTemplate<String, String> redisTemplateString(RedisConnectionFactory factiory){
		RedisTemplate<String,String> template=new RedisTemplate<>();
		template.setConnectionFactory(factiory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());
		return template;
	}
	
}


