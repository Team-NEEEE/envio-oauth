package io.envio.auth.common.security.oauth;

import java.time.Duration;
import java.util.Optional;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthorizationRequestRedisStore {

	private final AuthorizationRequestRedisRepository authorizationRequestRedisRepository;
	private final AuthorizationRequestSerializer authorizationRequestSerializer;

	public void save(
		final String requestId,
		final OAuth2AuthorizationRequest authorizationRequest,
		final Duration ttl
	) {
		String authorizationRequestJson = authorizationRequestSerializer.serialize(authorizationRequest);
		AuthorizationRequestRedisEntity entity = AuthorizationRequestRedisEntity.of(
			requestId,
			authorizationRequestJson,
			ttl
		);
		authorizationRequestRedisRepository.save(entity);
	}

	public Optional<OAuth2AuthorizationRequest> find(final String requestId) {
		return authorizationRequestRedisRepository.findById(requestId)
			.map(AuthorizationRequestRedisEntity::getAuthorizationRequestJson)
			.map(authorizationRequestSerializer::deserialize);
	}

	public void delete(final String requestId) {
		authorizationRequestRedisRepository.deleteById(requestId);
	}
}
