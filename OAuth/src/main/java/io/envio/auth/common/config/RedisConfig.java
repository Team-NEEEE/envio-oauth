package io.envio.auth.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.envio.auth.common.security.oauth.AuthorizationRequestRedisRepository;
import io.envio.auth.domain.cli.repository.RedisCliSessionRepository;

@Configuration
@EnableRedisRepositories(basePackageClasses = {
	AuthorizationRequestRedisRepository.class,
	RedisCliSessionRepository.class
})
public class RedisConfig {

	@Bean
	public LettuceConnectionFactory lettuceConnectionFactory(
		@Value("${spring.data.redis.host}") final String host,
		@Value("${spring.data.redis.port}") final int port,
		@Value("${spring.data.redis.password:}") final String password
	) {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, port);
		if (!password.isBlank()) {
			redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
		}

		return new LettuceConnectionFactory(redisStandaloneConfiguration);
	}

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
