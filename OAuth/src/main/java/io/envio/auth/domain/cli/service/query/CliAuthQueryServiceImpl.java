package io.envio.auth.domain.cli.service.query;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.common.error.exception.BusinessException;
import io.envio.auth.domain.cli.entity.RedisCliSession;
import io.envio.auth.domain.cli.repository.RedisCliSessionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliAuthQueryServiceImpl implements CliAuthQueryService {

	private static final Duration SAVE_LOCK_TTL = Duration.ofSeconds(300);
	private static final String SAVE_LOCK_KEY_PREFIX = "cli_session_save_lock:";

	private final RedisCliSessionRepository redisCliSessionRepository;
	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public RedisCliSession getSession(final String loginSessionId) {
		return redisCliSessionRepository.findById(loginSessionId)
			.orElseThrow(() -> new BusinessException(ErrorCode.CLI_LOGIN_SESSION_INVALID));
	}

	@Override
	public RedisCliSession reserveSessionForSave(final String loginSessionId, final String reqGithubId) {
		if (!tryAcquireSaveLock(loginSessionId)) {
			throw new BusinessException(ErrorCode.CLI_LOGIN_SESSION_ALREADY_PROCESSING);
		}

		try {
			RedisCliSession session = getSession(loginSessionId);
			validateSessionForSave(loginSessionId, reqGithubId, session);
			return session;
		} catch (RuntimeException exception) {
			releaseSessionSaveReservation(loginSessionId);
			throw exception;
		}
	}

	@Override
	public void deleteSession(final String loginSessionId) {
		redisCliSessionRepository.deleteById(loginSessionId);
		releaseSessionSaveReservation(loginSessionId);
		log.info("[CliAuth] login session deleted - sessionId: {}", loginSessionId);
	}

	@Override
	public void releaseSessionSaveReservation(final String loginSessionId) {
		stringRedisTemplate.delete(saveLockKey(loginSessionId));
	}

	private void validateSessionForSave(
		final String loginSessionId,
		final String reqGithubId,
		final RedisCliSession session
	) {
		if (!RedisCliSession.STATUS_SUCCESS.equals(session.getStatus())) {
			throw new BusinessException(ErrorCode.CLI_LOGIN_SESSION_NOT_READY);
		}
		if (reqGithubId == null || reqGithubId.isBlank() || !session.getGithubId().equals(reqGithubId)) {
			log.error("[CliAuth] github id mismatch - sessionId: {}, requestId: {}, sessionIdValue: {}",
				loginSessionId, reqGithubId, session.getGithubId());
			throw new BusinessException(ErrorCode.CLI_LOGIN_SESSION_MISMATCH);
		}
	}

	private boolean tryAcquireSaveLock(final String loginSessionId) {
		Boolean acquired = stringRedisTemplate.opsForValue()
			.setIfAbsent(saveLockKey(loginSessionId), "1", SAVE_LOCK_TTL);
		return Boolean.TRUE.equals(acquired);
	}

	private String saveLockKey(final String loginSessionId) {
		return SAVE_LOCK_KEY_PREFIX + loginSessionId;
	}
}
