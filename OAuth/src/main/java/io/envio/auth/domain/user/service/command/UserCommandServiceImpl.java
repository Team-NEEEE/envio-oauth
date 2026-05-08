package io.envio.auth.domain.user.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCommandServiceImpl implements UserCommandService {

	private final UserRepository userRepository;

	@Override
	public User save(final User user) {
		User savedUser = userRepository.save(user);
		log.info("[User] 사용자 저장 성공 - userId: {}, githubId: {}", savedUser.getId(), savedUser.getGithubId());
		return savedUser;
	}
}
