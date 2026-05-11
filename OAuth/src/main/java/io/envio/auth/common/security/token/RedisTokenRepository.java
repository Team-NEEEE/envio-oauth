package io.envio.auth.common.security.token;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository implements TokenRepository {

	private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh-token:";

	private final RedisTemplate<String, String> redisTemplate;

	@Override
	public void save(final String key, final String refreshToken, final Duration ttl) {
		redisTemplate.opsForValue().set(createKey(key), refreshToken, ttl);
	}

	@Override
	public Optional<String> find(final String key) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(createKey(key)));
	}

	@Override
	public void delete(final String key) {
		redisTemplate.delete(createKey(key));
	}

	private String createKey(final String key) {
		return REFRESH_TOKEN_KEY_PREFIX + key;
	}
}
