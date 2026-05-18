package io.envio.auth.common.security.oauth;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import io.envio.auth.common.config.properties.OAuth2UriProperties;
import io.envio.auth.domain.view.dto.response.OAuthLoginResDto;
import io.envio.auth.domain.view.service.ViewAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final ViewAuthService viewAuthService;
	private final OAuth2UriProperties oauth2UriProperties;

	@Override
	public void onAuthenticationSuccess(
		@NonNull final HttpServletRequest request,
		@NonNull final HttpServletResponse response,
		@NonNull final Authentication authentication
	) throws IOException {
		OAuthLoginResDto responseBody = viewAuthService.issueOAuthLoginTokens(authentication);

		response.sendRedirect(buildFrontendRedirectUri(responseBody));
	}

	private String buildFrontendRedirectUri(final OAuthLoginResDto responseBody) {
		String fragment = UriComponentsBuilder.newInstance()
			.queryParam("accessToken", responseBody.accessToken())
			.queryParam("refreshToken", responseBody.refreshToken())
			.queryParam("userId", responseBody.userId())
			.queryParam("email", responseBody.email())
			.queryParam("role", responseBody.role())
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
