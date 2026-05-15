package io.envio.auth.domain.view.dto.request;

import io.envio.auth.domain.user.entity.UserRole;

import jakarta.validation.constraints.NotNull;

public record AuthProjectMemberRoleUpdateReqDto(
	@NotNull(message = "role은 필수입니다.")
	UserRole role
) {
}
