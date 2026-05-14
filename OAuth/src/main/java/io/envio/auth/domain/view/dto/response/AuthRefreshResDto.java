package io.envio.auth.domain.view.dto.response;

public record AuthRefreshResDto(
	String accessToken,
	String refreshToken
) {
}
