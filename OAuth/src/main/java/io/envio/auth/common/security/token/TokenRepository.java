package io.envio.auth.common.security.token;

import java.time.Duration;
import java.util.Optional;

public interface TokenRepository {

	void save(final String key, final String refreshToken, final Duration ttl);

	Optional<String> find(final String key);

	Optional<String> findAndDelete(final String key);

	void delete(final String key);
}
