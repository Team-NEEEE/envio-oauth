package io.envio.auth.domain.cli.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Schema(description = "사용자 기기 및 공개키 등록 요청 DTO")
@Builder
public record PublicKeyRegisterReqDto(
	@Schema(description = "1회용 등록 티켓 (API 004에서 받은 값 제출)", example = "temp_1234567890abcdef...")
	@NotBlank(message = "등록 티켓(Token)은 필수입니다.")
	String registrationToken,

	@Schema(description = "SSH 공개키", example = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQC...")
	@NotBlank(message = "공개키는 필수 입력값입니다.")
	String publicKey,

	@Schema(description = "기기 이름", example = "Mingi-MacBook-Pro")
	@NotBlank(message = "기기 이름은 필수 입력값입니다.")
	@Size(max = 255, message = "기기 이름은 255자 이하로 입력해야 합니다.")
	String deviceName
) {
}