package io.envio.auth.common.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.envio.auth.common.config.properties.JwtProperties;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

	private static final String SECRET = "test-secret-key-for-jwt-token-provider";
	private static final Duration ACCESS_TOKEN_EXPIRATION = Duration.ofMinutes(30);
	private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(14);

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
		new JwtProperties(SECRET, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION),
		objectMapper
	);

	@Test
	@DisplayName("accessToken을 생성하면 JWT payload에 사용자 정보와 access 타입이 포함된다")
	void createAccessTokenContainsUserClaims() throws IOException {
		// given
		final Long userId = 1L;
		final String githubId = "123456";
		final String email = "user@example.com";
		final String role = "VIEWER";

		// when
		final String token = jwtTokenProvider.createAccessToken(userId, githubId, email, role);

		// then
		final String[] tokenParts = token.split("\\.");
		final JsonNode header = decodeTokenPart(tokenParts[0]);
		final JsonNode payload = decodeTokenPart(tokenParts[1]);

		assertEquals(3, tokenParts.length);
		assertEquals("HS256", header.get("alg").asText());
		assertEquals("JWT", header.get("typ").asText());
		assertEquals(String.valueOf(userId), payload.get("sub").asText());
		assertEquals(userId, payload.get("userId").asLong());
		assertEquals(githubId, payload.get("githubId").asText());
		assertEquals(email, payload.get("email").asText());
		assertEquals(role, payload.get("role").asText());
		assertEquals("access", payload.get("tokenType").asText());
		assertNotNull(payload.get("iat"));
		assertNotNull(payload.get("exp"));
		assertEquals(ACCESS_TOKEN_EXPIRATION.toSeconds(), payload.get("exp").asLong() - payload.get("iat").asLong());
	}

	@Test
	@DisplayName("refreshToken을 생성하면 JWT payload에 refresh 타입과 refresh 만료 시간이 포함된다")
	void createRefreshTokenContainsRefreshTypeAndExpiration() throws IOException {
		// given
		final Long userId = 1L;
		final String githubId = "123456";
		final String email = "user@example.com";
		final String role = "VIEWER";

		// when
		final String token = jwtTokenProvider.createRefreshToken(userId, githubId, email, role);

		// then
		final String[] tokenParts = token.split("\\.");
		final JsonNode payload = decodeTokenPart(tokenParts[1]);

		assertEquals(3, tokenParts.length);
		assertEquals("refresh", payload.get("tokenType").asText());
		assertEquals(REFRESH_TOKEN_EXPIRATION.toSeconds(), payload.get("exp").asLong() - payload.get("iat").asLong());
	}

	@Test
	@DisplayName("동일한 payload라도 secret이 다르면 JWT signature가 달라진다")
	void createTokenUsesSecretForSignature() {
		// given
		final JwtTokenProvider otherSecretTokenProvider = new JwtTokenProvider(
			new JwtProperties("other-secret-key-for-jwt-token-provider", ACCESS_TOKEN_EXPIRATION,
				REFRESH_TOKEN_EXPIRATION),
			objectMapper
		);

		// when
		final String token = jwtTokenProvider.createAccessToken(1L, "123456", "user@example.com", "VIEWER");
		final String otherSecretToken = otherSecretTokenProvider.createAccessToken(1L, "123456", "user@example.com",
			"VIEWER");

		// then
		final String signature = token.split("\\.")[2];
		final String otherSecretSignature = otherSecretToken.split("\\.")[2];

		assertFalse(signature.isBlank());
		assertFalse(otherSecretSignature.isBlank());
		assertNotEquals(signature, otherSecretSignature);
	}

	@Test
	@DisplayName("유효한 accessToken을 파싱하면 JWT claims를 반환한다")
	void parseAccessTokenReturnsClaims() {
		// given
		final String token = jwtTokenProvider.createAccessToken(1L, "123456", "user@example.com", "VIEWER");

		// when
		final JwtClaims claims = jwtTokenProvider.parseAccessToken(token);

		// then
		assertEquals(1L, claims.userId());
		assertEquals("123456", claims.githubId());
		assertEquals("user@example.com", claims.email());
		assertEquals("VIEWER", claims.role());
	}

	@Test
	@DisplayName("유효한 refreshToken을 파싱하면 JWT claims를 반환한다")
	void parseRefreshTokenReturnsClaims() {
		// given
		final String token = jwtTokenProvider.createRefreshToken(1L, "123456", "user@example.com", "VIEWER");

		// when
		final JwtClaims claims = jwtTokenProvider.parseRefreshToken(token);

		// then
		assertEquals(1L, claims.userId());
		assertEquals("123456", claims.githubId());
		assertEquals("user@example.com", claims.email());
		assertEquals("VIEWER", claims.role());
	}

	@Test
	@DisplayName("refreshToken을 accessToken으로 파싱하면 예외를 던진다")
	void parseAccessTokenThrowsExceptionWhenTokenTypeIsRefresh() {
		// given
		final String refreshToken = jwtTokenProvider.createRefreshToken(1L, "123456", "user@example.com", "VIEWER");

		// when & then
		assertThrows(JwtParsingException.class, () -> jwtTokenProvider.parseAccessToken(refreshToken));
	}

	@Test
	@DisplayName("accessToken을 refreshToken으로 파싱하면 예외를 던진다")
	void parseRefreshTokenThrowsExceptionWhenTokenTypeIsAccess() {
		// given
		final String accessToken = jwtTokenProvider.createAccessToken(1L, "123456", "user@example.com", "VIEWER");

		// when & then
		assertThrows(JwtParsingException.class, () -> jwtTokenProvider.parseRefreshToken(accessToken));
	}

	@Test
	@DisplayName("signature가 변조된 JWT를 파싱하면 예외를 던진다")
	void parseAccessTokenThrowsExceptionWhenSignatureIsInvalid() {
		// given
		final String token = jwtTokenProvider.createAccessToken(1L, "123456", "user@example.com", "VIEWER");
		final String[] tokenParts = token.split("\\.");
		final String forgedToken = tokenParts[0] + "." + tokenParts[1] + ".forged-signature";

		// when & then
		assertThrows(JwtParsingException.class, () -> jwtTokenProvider.parseAccessToken(forgedToken));
	}

	@Test
	@DisplayName("필수 클레임이 누락된 JWT를 파싱하면 JwtParsingException을 던진다")
	void parseAccessTokenThrowsExceptionWhenRequiredClaimIsMissing() throws IOException {
		// given
		final String token = createTokenWithoutEmailClaim();

		// when & then
		assertThrows(JwtParsingException.class, () -> jwtTokenProvider.parseAccessToken(token));
	}

	private JsonNode decodeTokenPart(final String tokenPart) throws IOException {
		final byte[] decoded = Base64.getUrlDecoder().decode(tokenPart);
		return objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8));
	}

	private String createTokenWithoutEmailClaim() throws IOException {
		final Instant now = Instant.now();
		final String encodedHeader = encodeJson(Map.of(
			"alg", "HS256",
			"typ", "JWT"
		));
		final String encodedPayload = encodeJson(Map.of(
			"sub", "1",
			"userId", 1L,
			"githubId", "123456",
			"role", "VIEWER",
			"tokenType", "access",
			"iat", now.getEpochSecond(),
			"exp", now.plus(ACCESS_TOKEN_EXPIRATION).getEpochSecond()
		));
		final String signature = sign(encodedHeader + "." + encodedPayload);
		return encodedHeader + "." + encodedPayload + "." + signature;
	}

	private String encodeJson(final Map<String, Object> value) throws IOException {
		return Base64.getUrlEncoder()
			.withoutPadding()
			.encodeToString(objectMapper.writeValueAsBytes(value));
	}

	private String sign(final String value) {
		try {
			final Mac mac = Mac.getInstance("HmacSHA256");
			final SecretKeySpec secretKeySpec = new SecretKeySpec(
				SECRET.getBytes(StandardCharsets.UTF_8),
				"HmacSHA256"
			);
			mac.init(secretKeySpec);
			return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		} catch (java.security.GeneralSecurityException exception) {
			throw new IllegalStateException("Failed to sign JWT.", exception);
		}
	}
}
