package io.envio.auth.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Schema(description = "사용자 생성 요청 DTO")
@Builder
public record UserCreateReqDto(
	@Schema(description = "GitHub 사용자 ID", example = "123456")
	@NotBlank(message = "GitHub 사용자 ID는 필수입니다.")
	@Size(max = 255, message = "GitHub 사용자 ID는 255자 이하로 입력해야 합니다.")
	String githubId,

	@Schema(description = "이메일", example = "user@example.com")
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@Size(max = 255, message = "이메일은 255자 이하로 입력해야 합니다.")
	String email
) {
}
