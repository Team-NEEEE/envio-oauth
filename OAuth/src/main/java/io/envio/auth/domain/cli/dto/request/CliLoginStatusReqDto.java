package io.envio.auth.domain.cli.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "CLI 로그인 상태 확인 요청 DTO")
public record CliLoginStatusReqDto(
	@Schema(description = "로그인 세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	@NotBlank(message = "세션 ID는 필수입니다.")
	String loginSessionId
) {
}