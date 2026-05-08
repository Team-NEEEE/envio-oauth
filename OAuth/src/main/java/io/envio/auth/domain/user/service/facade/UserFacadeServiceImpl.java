package io.envio.auth.domain.user.service.facade;

import org.springframework.stereotype.Service;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.domain.user.converter.UserConverter;
import io.envio.auth.domain.user.dto.request.UserCreateReqDto;
import io.envio.auth.domain.user.dto.response.UserResDto;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.exception.UserException;
import io.envio.auth.domain.user.service.command.UserCommandService;
import io.envio.auth.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFacadeServiceImpl implements UserFacadeService {

	private final UserCommandService commandService;
	private final UserQueryService queryService;

	@Override
	public UserResDto createUser(final UserCreateReqDto reqDto) {
		validateGithubId(reqDto.githubId());
		User user = commandService.create(reqDto);
		return UserConverter.toUserResDto(user);
	}

	@Override
	public UserResDto getUser(final Long userId) {
		User user = queryService.findById(userId);
		return UserConverter.toUserResDto(user);
	}

	@Override
	public UserResDto getUserByGithubId(final String githubId) {
		User user = queryService.findByGithubId(githubId);
		return UserConverter.toUserResDto(user);
	}

	private void validateGithubId(final String githubId) {
		if (queryService.existsByGithubId(githubId)) {
			throw new UserException(ErrorCode.USER_ALREADY_EXISTS);
		}
	}
}
