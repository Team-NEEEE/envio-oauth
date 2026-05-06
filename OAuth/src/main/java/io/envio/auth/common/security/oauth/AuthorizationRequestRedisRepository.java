package io.envio.auth.common.security.oauth;

import org.springframework.data.repository.CrudRepository;

public interface AuthorizationRequestRedisRepository
	extends CrudRepository<AuthorizationRequestRedisEntity, String> {
}
