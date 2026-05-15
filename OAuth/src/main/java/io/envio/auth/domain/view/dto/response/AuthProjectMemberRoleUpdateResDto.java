package io.envio.auth.domain.view.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.envio.auth.domain.user.entity.UserRole;

import lombok.Builder;

@Builder
public record AuthProjectMemberRoleUpdateResDto(
	@JsonProperty("project_id")
	Long projectId,

	@JsonProperty("user_id")
	Long userId,

	UserRole role,

	@JsonProperty("updated_at")
	LocalDateTime updatedAt
) {
}
