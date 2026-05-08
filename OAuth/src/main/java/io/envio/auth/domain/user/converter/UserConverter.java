package io.envio.auth.domain.user.converter;

import io.envio.auth.domain.user.dto.request.UserCreateReqDto;
import io.envio.auth.domain.user.dto.response.UserResDto;
import io.envio.auth.domain.user.entity.User;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserConverter {

	public User toEntity(final UserCreateReqDto reqDto) {
		return User.createGithubUser(reqDto.githubId(), reqDto.email());
	}

	public UserResDto toUserResDto(final User user) {
		return UserResDto.builder()
			.userId(user.getId())
			.githubId(user.getGithubId())
			.email(user.getEmail())
			.role(user.getRole().name())
			.build();
	}
}
