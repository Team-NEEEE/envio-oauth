package io.envio.auth.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "사용자 응답 DTO")
@Builder
public record UserResDto(
	@Schema(description = "사용자 ID", example = "1")
	Long userId,

	@Schema(description = "GitHub 사용자 ID", example = "123456")
	String githubId,

	@Schema(description = "이메일", example = "user@example.com")
	String email,

	@Schema(description = "사용자 역할", example = "USER")
	String role
) {
}
