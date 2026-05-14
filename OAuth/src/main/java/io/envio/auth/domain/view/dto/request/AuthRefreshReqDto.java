package io.envio.auth.domain.view.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthRefreshReqDto(
	@NotBlank(message = "refreshToken은 필수입니다.")
	String refreshToken
) {
}
