package io.envio.auth.domain.cli.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Schema(description = "CLI 로그인 유저 및 기기 등록 요청 DTO")
@Builder
public record CliLoginSaveReqDto(
	@Schema(description = "인증이 확정된 세션 ID (API 001에서 받은 값)", example = "550e8400-e29b-41d4-a716-446655440000")
	@NotBlank(message = "세션 ID는 필수입니다.")
	String loginSessionId,

	@Schema(description = "사용자의 GitHub ID", example = "mingi-lee")
	@NotBlank(message = "GitHub ID는 필수입니다.")
	String githubId,

	@Schema(description = "사용자의 GitHub 이메일", example = "mingi.lee@example.com")
	String email,

	@Schema(description = "기기 이름", example = "Mingi-MacBook-Pro")
	@NotBlank(message = "기기 이름은 필수입니다.")
	@Size(max = 255, message = "기기 이름은 255자 이하로 입력해야 합니다.")
	String deviceName,

	@Schema(description = "통신에 사용할 SSH 공개키", example = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQC...")
	@NotBlank(message = "공개키는 필수입니다.")
	String publicKey
) {
}