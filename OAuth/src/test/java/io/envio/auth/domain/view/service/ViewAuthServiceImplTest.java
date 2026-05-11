package io.envio.auth.domain.view.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import io.envio.auth.common.config.properties.JwtProperties;
import io.envio.auth.common.security.jwt.JwtTokenProvider;
import io.envio.auth.common.security.token.TokenRepository;
import io.envio.auth.domain.view.dto.response.OAuthLoginResDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("ViewAuthService")
class ViewAuthServiceImplTest {

	private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(14);

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private JwtProperties jwtProperties;

	@Mock
	private TokenRepository tokenRepository;

	@Mock
	private Authentication authentication;

	@Mock
	private OAuth2User oauth2User;

	@InjectMocks
	private ViewAuthServiceImpl viewAuthService;

	@Test
	@DisplayName("OAuth 인증 정보로 accessToken과 refreshToken을 발급하고 refreshToken을 저장한다")
	void issueOAuthLoginTokensCreatesTokensAndStoresRefreshToken() {
		// given
		when(authentication.getPrincipal()).thenReturn(oauth2User);
		when(oauth2User.getAttribute("userId")).thenReturn(1L);
		when(oauth2User.getAttribute("githubId")).thenReturn("123456");
		when(oauth2User.getAttribute("email")).thenReturn("user@example.com");
		when(oauth2User.getAttribute("role")).thenReturn("VIEWER");
		when(jwtTokenProvider.createAccessToken(1L, "123456", "user@example.com", "VIEWER"))
			.thenReturn("access-token");
		when(jwtTokenProvider.createRefreshToken(1L, "123456", "user@example.com", "VIEWER"))
			.thenReturn("refresh-token");
		when(jwtProperties.refreshTokenExpiration()).thenReturn(REFRESH_TOKEN_EXPIRATION);

		// when
		final OAuthLoginResDto result = viewAuthService.issueOAuthLoginTokens(authentication);

		// then
		assertEquals("access-token", result.accessToken());
		assertEquals("refresh-token", result.refreshToken());
		assertEquals(1L, result.userId());
		assertEquals("user@example.com", result.email());
		assertEquals("VIEWER", result.role());
		verify(tokenRepository).save("1", "refresh-token", REFRESH_TOKEN_EXPIRATION);
	}
}
