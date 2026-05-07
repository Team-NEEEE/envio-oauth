package io.envio.auth.domain.user.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.domain.user.converter.UserConverter;
import io.envio.auth.domain.user.dto.request.UserCreateReqDto;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.exception.UserException;
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
	public User create(final UserCreateReqDto reqDto) {
		validateGithubId(reqDto.githubId());
		User user = userRepository.save(UserConverter.toEntity(reqDto));
		log.info("[User] 사용자 생성 성공 - userId: {}, githubId: {}", user.getId(), user.getGithubId());
		return user;
	}

	private void validateGithubId(final String githubId) {
		if (userRepository.existsByGithubId(githubId)) {
			throw new UserException(ErrorCode.USER_ALREADY_EXISTS);
		}
	}
}
