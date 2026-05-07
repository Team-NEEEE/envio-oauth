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
			// 커스텀 예외 클래스가 있다면 교체해 주세요 (예: new CliAuthException(ErrorCode.SESSION_NOT_FOUND))
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 세션입니다."));
	}

	@Override
	public void validateSessionForSave(final String loginSessionId, final String reqGithubId) {
		RedisCliSession session = getSession(loginSessionId);

		if (!"SUCCESS".equals(session.getStatus())) {
			throw new IllegalStateException("아직 GitHub 인증이 완료되지 않은 세션입니다.");
		}

		if (!session.getGithubId().equals(reqGithubId)) {
			log.error("[CliAuth] 소유자 불일치 - sessionId: {}, 요청 ID: {}, 실제 ID: {}",
				loginSessionId, reqGithubId, session.getGithubId());
			throw new IllegalArgumentException("GitHub 인증 정보가 일치하지 않습니다.");
		}
	}
}