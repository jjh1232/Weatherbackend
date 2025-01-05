package com.example.firstproject.configure;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class Redisconfig {

	@Value("${spring.data.redis.host}")
	private String host;
	
	@Value("${spring.data.redis.port}")
	private int port;
	
	
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
		return new LettuceConnectionFactory(host,port);
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

}


