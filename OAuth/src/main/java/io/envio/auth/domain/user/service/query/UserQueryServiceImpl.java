package io.envio.auth.domain.user.service.query;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.exception.UserException;
import io.envio.auth.domain.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQueryServiceImpl implements UserQueryService {

	private final UserRepository userRepository;

	@Override
	public User findById(final Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
		log.info("[User] 사용자 조회 성공 - userId: {}", userId);
		return user;
	}

	@Override
	public User findByGithubId(final String githubId) {
		User user = userRepository.findByGithubId(githubId)
			.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
		log.info("[User] 사용자 조회 성공 - githubId: {}", githubId);
		return user;
	}

	@Override
	public Optional<User> findOptionalByGithubId(final String githubId) {
		return userRepository.findByGithubId(githubId);
	}

	@Override
	public boolean existsByGithubId(final String githubId) {
		return userRepository.existsByGithubId(githubId);
	}
}
