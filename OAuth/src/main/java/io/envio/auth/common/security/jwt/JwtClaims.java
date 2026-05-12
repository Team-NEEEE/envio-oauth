package io.envio.auth.common.security.jwt;

public record JwtClaims(
	Long userId,
	String githubId,
	String email,
	String role
) {
}
