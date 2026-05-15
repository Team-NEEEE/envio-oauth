package io.envio.auth.domain.user.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserRole;
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

	@Override
	public User updateRole(final User user, final UserRole role) {
		UserRole previousRole = user.getRole();
		user.updateRole(role);
		User savedUser = userRepository.saveAndFlush(user);
		log.info("[User] 사용자 역할 변경 처리 - userId: {}, role: {} -> {}", savedUser.getId(), previousRole, role);
		return savedUser;
	}
}
