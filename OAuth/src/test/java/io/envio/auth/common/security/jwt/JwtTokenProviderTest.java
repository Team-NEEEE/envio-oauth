package io.envio.auth.common.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

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

	private JsonNode decodeTokenPart(final String tokenPart) throws IOException {
		final byte[] decoded = Base64.getUrlDecoder().decode(tokenPart);
		return objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8));
	}
}
