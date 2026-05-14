package io.envio.auth.domain.view.dto.response;

import lombok.Builder;

@Builder
public record AuthRefreshResDto(
	String accessToken,
	String refreshToken
) {
}
