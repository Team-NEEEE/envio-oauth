package io.envio.auth.common.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityConstants {

	public static final String[] PUBLIC_URLS = {
		"/api/auth/oauth/**",
		"/api/auth/cli/login/**",
		"/api/auth/refresh",
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/actuator/health"
	};

	public static final String[] AUTHENTICATED_URLS = {
		"/api/auth/me",
		"/api/auth/logout",
		"/api/auth/projects/**"
	};

	public static final String[] JWT_EXCLUDE_URLS = {
		"/api/auth/oauth/**",
		"/api/auth/cli/login/**",
		"/api/auth/refresh"
	};
}
