package io.envio.auth.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.envio.auth.common.security.oauth.AuthorizationRequestRedisRepository;

@Configuration
@EnableRedisRepositories(basePackageClasses = AuthorizationRequestRedisRepository.class)
public class RedisConfig {

	@Bean
	public RedisTemplate<String, String> redisTemplate(final RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(stringRedisSerializer);
		redisTemplate.setHashKeySerializer(stringRedisSerializer);
		redisTemplate.setValueSerializer(stringRedisSerializer);
		redisTemplate.setHashValueSerializer(stringRedisSerializer);
		redisTemplate.afterPropertiesSet();

		return redisTemplate;
	}
}
