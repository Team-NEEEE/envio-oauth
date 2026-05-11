package io.envio.auth.domain.cli.service.query;

import io.envio.auth.domain.cli.entity.RedisCliSession;

public interface CliAuthQueryService {
	RedisCliSession getSession(final String loginSessionId);

	RedisCliSession validateSessionForSave(final String loginSessionId, final String reqGithubId);
}
