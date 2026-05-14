package io.envio.auth.domain.view.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthRefreshReqDto(
	@NotBlank
	String refreshToken
) {
}
