package io.envio.auth.domain.cli.service.query;

import io.envio.auth.domain.cli.entity.RedisCliSession;

public interface CliAuthQueryService {
	RedisCliSession getSession(final String loginSessionId);

	RedisCliSession reserveSessionForSave(final String loginSessionId, final String reqGithubId);

	void deleteSession(final String loginSessionId);

	void releaseSessionSaveReservation(final String loginSessionId);
}
