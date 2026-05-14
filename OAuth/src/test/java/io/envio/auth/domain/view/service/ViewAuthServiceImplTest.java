package io.envio.auth.domain.view.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import io.envio.auth.common.config.properties.JwtProperties;
import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.common.error.exception.BusinessException;
import io.envio.auth.common.security.jwt.JwtClaims;
import io.envio.auth.common.security.jwt.JwtParsingException;
import io.envio.auth.common.security.jwt.JwtTokenProvider;
import io.envio.auth.common.security.token.TokenRepository;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserDevice;
import io.envio.auth.domain.user.entity.UserRole;
import io.envio.auth.domain.user.service.query.UserDeviceQueryService;
import io.envio.auth.domain.user.service.query.UserQueryService;
import io.envio.auth.domain.view.dto.request.AuthRefreshReqDto;
import io.envio.auth.domain.view.dto.response.AuthMeResDto;
import io.envio.auth.domain.view.dto.response.AuthRefreshResDto;
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
	private UserQueryService userQueryService;

	@Mock
	private UserDeviceQueryService userDeviceQueryService;

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

	@Test
	@DisplayName("OAuth 인증 정보에 필수 attribute가 없으면 명확한 예외를 던진다")
	void issueOAuthLoginTokensThrowsExceptionWhenRequiredAttributeIsMissing() {
		// given
		when(authentication.getPrincipal()).thenReturn(oauth2User);
		when(oauth2User.getAttribute("userId")).thenReturn(null);

		// when
		final IllegalStateException exception = assertThrows(
			IllegalStateException.class,
			() -> viewAuthService.issueOAuthLoginTokens(authentication)
		);

		// then
		assertEquals("userId attribute is missing from OAuth2User", exception.getMessage());
	}

	@Test
	@DisplayName("JWT claims의 사용자 ID로 현재 사용자 정보를 조회한다")
	void getCurrentUserReturnsAuthenticatedUser() {
		// given
		final JwtClaims claims = new JwtClaims(1L, "123456", "user@example.com", "VIEWER");
		final User user = createUser();
		final UserDevice userDevice = createUserDevice(user);
		when(userQueryService.findById(1L)).thenReturn(user);
		when(userDeviceQueryService.findLatestByUserId(1L)).thenReturn(Optional.of(userDevice));

		// when
		final AuthMeResDto result = viewAuthService.getCurrentUser(claims);

		// then
		assertEquals(1L, result.userId());
		assertEquals("123456", result.githubId());
		assertEquals("user@example.com", result.email());
		assertEquals("VIEWER", result.role());
		assertEquals("ssh-rsa AAAAB3", result.publicKey());
	}

	@Test
	@DisplayName("디바이스가 없는 사용자는 publicKey가 null인 응답을 반환한다")
	void getCurrentUserReturnsNullPublicKeyWhenNoDeviceRegistered() {
		// given
		final JwtClaims claims = new JwtClaims(1L, "123456", "user@example.com", "VIEWER");
		final User user = createUser();
		when(userQueryService.findById(1L)).thenReturn(user);
		when(userDeviceQueryService.findLatestByUserId(1L)).thenReturn(Optional.empty());

		// when
		final AuthMeResDto result = viewAuthService.getCurrentUser(claims);

		// then
		assertEquals(1L, result.userId());
		assertNull(result.publicKey());
	}

	@Test
	@DisplayName("저장된 refreshToken이 일치하면 새 accessToken과 refreshToken을 발급한다")
	void refreshTokenReturnsNewTokensWhenStoredTokenMatches() {
		// given
		final AuthRefreshReqDto reqDto = new AuthRefreshReqDto("refresh-token");
		final JwtClaims claims = new JwtClaims(1L, "123456", "user@example.com", "VIEWER");
		final User user = createUser();
		when(jwtTokenProvider.parseRefreshToken("refresh-token")).thenReturn(claims);
		when(tokenRepository.find("1")).thenReturn(Optional.of("refresh-token"));
		when(userQueryService.findById(1L)).thenReturn(user);
		when(jwtTokenProvider.createAccessToken(1L, "123456", "user@example.com", "VIEWER"))
			.thenReturn("new-access-token");
		when(jwtTokenProvider.createRefreshToken(1L, "123456", "user@example.com", "VIEWER"))
			.thenReturn("new-refresh-token");
		when(jwtProperties.refreshTokenExpiration()).thenReturn(REFRESH_TOKEN_EXPIRATION);

		// when
		final AuthRefreshResDto result = viewAuthService.refreshToken(reqDto);

		// then
		assertEquals("new-access-token", result.accessToken());
		assertEquals("new-refresh-token", result.refreshToken());
		verify(tokenRepository).save("1", "new-refresh-token", REFRESH_TOKEN_EXPIRATION);
	}

	@Test
	@DisplayName("저장된 refreshToken과 요청 토큰이 다르면 인증 예외를 던진다")
	void refreshTokenThrowsExceptionWhenStoredTokenDoesNotMatch() {
		// given
		final AuthRefreshReqDto reqDto = new AuthRefreshReqDto("refresh-token");
		final JwtClaims claims = new JwtClaims(1L, "123456", "user@example.com", "VIEWER");
		when(jwtTokenProvider.parseRefreshToken("refresh-token")).thenReturn(claims);
		when(tokenRepository.find("1")).thenReturn(Optional.of("other-refresh-token"));

		// when
		final BusinessException exception = assertThrows(
			BusinessException.class,
			() -> viewAuthService.refreshToken(reqDto)
		);

		// then
		assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
	}

	@Test
	@DisplayName("refreshToken 파싱에 실패하면 인증 예외를 던진다")
	void refreshTokenThrowsExceptionWhenTokenParsingFails() {
		// given
		final AuthRefreshReqDto reqDto = new AuthRefreshReqDto("invalid-refresh-token");
		when(jwtTokenProvider.parseRefreshToken("invalid-refresh-token"))
			.thenThrow(new JwtParsingException("Invalid JWT."));

		// when
		final BusinessException exception = assertThrows(
			BusinessException.class,
			() -> viewAuthService.refreshToken(reqDto)
		);

		// then
		assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
	}

	private User createUser() {
		return User.builder()
			.id(1L)
			.githubId("123456")
			.email("user@example.com")
			.role(UserRole.VIEWER)
			.build();
	}

	private UserDevice createUserDevice(final User user) {
		return UserDevice.builder()
			.id(1L)
			.user(user)
			.deviceName("Mingi-MacBook-Pro")
			.publicKey("ssh-rsa AAAAB3")
			.build();
	}
}
