package io.envio.auth.domain.user.service.query;

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
	public User findByEmployeeNumber(final String employeeNumber) {
		User user = userRepository.findByEmployeeNumber(employeeNumber)
			.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
		log.info("[User] 사용자 조회 성공 - employeeNumber: {}", employeeNumber);
		return user;
	}
}
