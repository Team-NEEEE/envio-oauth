package io.envio.auth.common.security.oauth;

import java.time.Duration;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@RedisHash("oauth2:authorization-request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthorizationRequestRedisEntity {

	@Id
	private String requestId;

	private String authorizationRequestJson;

	@TimeToLive
	private Long expiration;

	private AuthorizationRequestRedisEntity(
		final String requestId,
		final String authorizationRequestJson,
		final Long expiration
	) {
		this.requestId = requestId;
		this.authorizationRequestJson = authorizationRequestJson;
		this.expiration = expiration;
	}

	public static AuthorizationRequestRedisEntity of(
		final String requestId,
		final String authorizationRequestJson,
		final Duration ttl
	) {
		return new AuthorizationRequestRedisEntity(
			requestId,
			authorizationRequestJson,
			ttl.toSeconds()
		);
	}
}
