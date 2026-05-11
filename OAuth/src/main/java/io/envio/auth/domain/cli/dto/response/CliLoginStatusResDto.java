package io.envio.auth.domain.cli.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "CLI 로그인 상태 확인 응답 DTO")
@Builder
public record CliLoginStatusResDto(
	@Schema(description = "로그인 세션 상태 (PENDING, SUCCESS, EXPIRED)", example = "SUCCESS")
	String status,

	@Schema(description = "GitHub 사용자 아이디", example = "123456")
	String githubId,

	@Schema(description = "GitHub 사용자 이메일", example = "mingi.lee@example.com")
	String email
) {
}
