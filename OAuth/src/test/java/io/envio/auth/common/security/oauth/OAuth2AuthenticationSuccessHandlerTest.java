package io.envio.auth.common.security.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import io.envio.auth.common.config.properties.OAuth2UriProperties;
import io.envio.auth.domain.view.dto.response.OAuthLoginResDto;
import io.envio.auth.domain.view.service.ViewAuthService;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2AuthenticationSuccessHandler")
class OAuth2AuthenticationSuccessHandlerTest {

	private static final String FRONTEND_REDIRECT_URI = "https://frontend.example.com/auth/callback";

	@Mock
	private ViewAuthService viewAuthService;

	@Mock
	private Authentication authentication;

	private OAuth2AuthenticationSuccessHandler successHandler;

	@BeforeEach
	void setUp() {
		OAuth2UriProperties oauth2UriProperties = new OAuth2UriProperties(
			"/api/auth/oauth",
			"/api/auth/oauth/*/callback",
			FRONTEND_REDIRECT_URI
		);
		successHandler = new OAuth2AuthenticationSuccessHandler(viewAuthService, oauth2UriProperties);
	}

	@Test
	@DisplayName("OAuth login success redirects to frontend callback")
	void onAuthenticationSuccessRedirectsToFrontendCallback() throws IOException {
		// given
		final OAuthLoginResDto loginResponse = OAuthLoginResDto.builder()
			.accessToken("access-token")
			.refreshToken("refresh-token")
			.userId(1L)
			.email("user@example.com")
			.role("VIEWER")
			.build();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		when(viewAuthService.issueOAuthLoginTokens(authentication)).thenReturn(loginResponse);

		// when
		successHandler.onAuthenticationSuccess(request, response, authentication);

		// then
		assertEquals(
			FRONTEND_REDIRECT_URI + "#accessToken=access-token&refreshToken=refresh-token"
				+ "&userId=1&email=user@example.com&role=VIEWER",
			response.getRedirectedUrl()
		);
		verify(viewAuthService).issueOAuthLoginTokens(authentication);
	}
}
