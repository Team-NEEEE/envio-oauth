package io.envio.auth.domain.user.converter;

import io.envio.auth.domain.user.dto.request.UserCreateReqDto;
import io.envio.auth.domain.user.dto.response.UserResDto;
import io.envio.auth.domain.user.entity.User;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserConverter {

	public User toEntity(final UserCreateReqDto reqDto) {
		return User.builder()
			.employeeNumber(reqDto.employeeNumber())
			.name(reqDto.name())
			.email(reqDto.email())
			.build();
	}

	public UserResDto toUserResDto(final User user) {
		return UserResDto.builder()
			.userId(user.getId())
			.employeeNumber(user.getEmployeeNumber())
			.name(user.getName())
			.email(user.getEmail())
			.build();
	}
}
