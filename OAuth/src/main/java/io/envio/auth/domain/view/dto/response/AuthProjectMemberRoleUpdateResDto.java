package io.envio.auth.domain.view.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record AuthProjectMemberRoleUpdateResDto(
	@JsonProperty("project_id")
	Long projectId,

	@JsonProperty("user_id")
	Long userId,

	String role,

	@JsonProperty("updated_at")
	LocalDateTime updatedAt
) {
}
