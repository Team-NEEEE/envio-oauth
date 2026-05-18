package io.envio.auth.common.security.oauth;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import io.envio.auth.common.config.properties.OAuth2UriProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

	private static final String OAUTH_FAILED = "oauth_failed";

	private final OAuth2UriProperties oauth2UriProperties;

	@Override
	public void onAuthenticationFailure(
		@NonNull final HttpServletRequest request,
		@NonNull final HttpServletResponse response,
		@NonNull final AuthenticationException exception
	) throws IOException {
		log.warn("OAuth2 authentication failed", exception);

		response.sendRedirect(buildFrontendFailureRedirectUri());
	}

	private String buildFrontendFailureRedirectUri() {
		String fragment = UriComponentsBuilder.newInstance()
			.queryParam("error", OAUTH_FAILED)
			.build()
			.encode()
			.toUriString()
			.substring(1);

		return UriComponentsBuilder.fromUriString(oauth2UriProperties.frontendRedirectUri())
			.fragment(fragment)
			.build()
			.toUriString();
	}
}
