package io.envio.auth.domain.view.dto.response;

import lombok.Builder;

@Builder
public record OAuthLoginResDto(
	String accessToken,
	String refreshToken,
	Long userId,
	String email,
	String role
) {
}
