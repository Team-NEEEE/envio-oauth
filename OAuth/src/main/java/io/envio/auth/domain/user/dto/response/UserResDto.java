package io.envio.auth.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "사용자 응답 DTO")
@Builder
public record UserResDto(
	@Schema(description = "사용자 ID", example = "1")
	Long userId,

	@Schema(description = "사번", example = "EMP001")
	String employeeNumber,

	@Schema(description = "사용자 이름", example = "홍길동")
	String name,

	@Schema(description = "이메일", example = "hong@example.com")
	String email
) {
}
