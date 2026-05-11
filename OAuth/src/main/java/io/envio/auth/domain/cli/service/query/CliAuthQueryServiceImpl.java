package io.envio.auth.domain.cli.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.auth.domain.cli.entity.RedisCliSession;
import io.envio.auth.domain.cli.repository.RedisCliSessionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliAuthQueryServiceImpl implements CliAuthQueryService {

	private final RedisCliSessionRepository redisCliSessionRepository;

	@Override
	public RedisCliSession getSession(final String loginSessionId) {
		return redisCliSessionRepository.findById(loginSessionId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid or expired login session."));
	}

	@Override
	public RedisCliSession validateSessionForSave(final String loginSessionId, final String reqGithubId) {
		RedisCliSession session = getSession(loginSessionId);

		if (!"SUCCESS".equals(session.getStatus())) {
			throw new IllegalStateException("GitHub authentication is not completed.");
		}

		if (reqGithubId == null || reqGithubId.isBlank() || !session.getGithubId().equals(reqGithubId)) {
			log.error("[CliAuth] github id mismatch - sessionId: {}, requestId: {}, sessionIdValue: {}",
				loginSessionId, reqGithubId, session.getGithubId());
			throw new IllegalArgumentException("GitHub authentication information does not match.");
		}

		return session;
	}
}
