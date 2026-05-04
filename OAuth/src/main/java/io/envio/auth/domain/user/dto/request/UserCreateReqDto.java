package io.envio.auth.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Schema(description = "사용자 생성 요청 DTO")
@Builder
public record UserCreateReqDto(
	@Schema(description = "사번", example = "EMP001")
	@NotBlank(message = "사번은 필수 입력값입니다.")
	@Size(max = 100, message = "사번은 100자 이하로 입력해야 합니다.")
	String employeeNumber,

	@Schema(description = "사용자 이름", example = "홍길동")
	@NotBlank(message = "사용자 이름은 필수 입력값입니다.")
	@Size(max = 50, message = "사용자 이름은 50자 이하로 입력해야 합니다.")
	String name,

	@Schema(description = "이메일", example = "hong@example.com")
	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@Size(max = 255, message = "이메일은 255자 이하로 입력해야 합니다.")
	String email
) {
}
