package io.envio.auth.domain.cli.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "CLI 로그인 및 기기 등록 완료 응답 DTO")
@Builder
public record CliLoginSaveResDto(
	@Schema(description = "DB에 저장된 유저의 고유 ID", example = "105")
	Long userId,

	@Schema(description = "DB에 저장된 기기의 고유 ID", example = "42")
	Long deviceId,

	@Schema(description = "GitHub 사용자 ID", example = "mingi-lee")
	String githubId,

	@Schema(description = "GitHub 이메일", example = "mingi.lee@example.com")
	String email
) {
}