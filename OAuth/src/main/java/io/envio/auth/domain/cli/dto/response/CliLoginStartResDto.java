package io.envio.auth.domain.cli.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "CLI 로그인 시작 응답 DTO")
@Builder
public record CliLoginStartResDto(
	@Schema(description = "로그인 세션 ID (상태 확인용)", example = "550e8400-e29b-41d4-a716-446655440000")
	String loginSessionId,

	@Schema(description = "GitHub OAuth 인증 URL", example = "https://github.com/login/oauth/authorize?client_id=...")
	String loginUrl
) {
}