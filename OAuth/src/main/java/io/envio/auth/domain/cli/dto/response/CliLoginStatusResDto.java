package io.envio.auth.domain.cli.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "CLI 로그인 상태 확인 응답 DTO")
@Builder
public record CliLoginStatusResDto(
	@Schema(description = "로그인 세션 상태 (PENDING, SUCCESS, EXPIRED)", example = "SUCCESS")
	String status,

	@Schema(description = "공개키 등록을 위한 1회용 임시 티켓 (SUCCESS 상태일 때만 발급)", example = "temp_1234567890abcdef...")
	String registrationToken
) {
}