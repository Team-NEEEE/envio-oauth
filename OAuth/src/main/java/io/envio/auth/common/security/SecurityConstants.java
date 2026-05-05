package io.envio.auth.common.security;

public final class SecurityConstants {

	public static final String[] PUBLIC_URLS = {
		"/api/auth/oauth/**",
		"/api/auth/refresh",
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/actuator/health"
	};

	public static final String[] JWT_EXCLUDE_URLS = {
		"/api/auth/oauth/**",
		"/api/auth/refresh"
	};

	private SecurityConstants() {
	}
}
