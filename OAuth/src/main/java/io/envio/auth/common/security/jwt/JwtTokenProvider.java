package io.envio.auth.common.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.envio.auth.common.config.properties.JwtProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final String TOKEN_TYPE_ACCESS = "access";
	private static final String TOKEN_TYPE_REFRESH = "refresh";

	private final JwtProperties jwtProperties;
	private final ObjectMapper objectMapper;

	public String createAccessToken(
		final Long userId,
		final String githubId,
		final String email,
		final String role
	) {
		return createToken(userId, githubId, email, role, TOKEN_TYPE_ACCESS, jwtProperties.accessTokenExpiration());
	}

	public String createRefreshToken(
		final Long userId,
		final String githubId,
		final String email,
		final String role
	) {
		return createToken(userId, githubId, email, role, TOKEN_TYPE_REFRESH, jwtProperties.refreshTokenExpiration());
	}

	private String createToken(
		final Long userId,
		final String githubId,
		final String email,
		final String role,
		final String tokenType,
		final Duration expiration
	) {
		Instant now = Instant.now();
		Map<String, Object> header = Map.of(
			"alg", "HS256",
			"typ", "JWT"
		);
		Map<String, Object> payload = Map.of(
			"sub", String.valueOf(userId),
			"userId", userId,
			"githubId", githubId,
			"email", email,
			"role", role,
			"tokenType", tokenType,
			"iat", now.getEpochSecond(),
			"exp", now.plus(expiration).getEpochSecond()
		);

		String encodedHeader = encodeJson(header);
		String encodedPayload = encodeJson(payload);
		String signature = sign(encodedHeader + "." + encodedPayload);
		return encodedHeader + "." + encodedPayload + "." + signature;
	}

	private String encodeJson(final Map<String, Object> value) {
		try {
			return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(objectMapper.writeValueAsBytes(value));
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to encode JWT.", exception);
		}
	}

	private String sign(final String value) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			SecretKeySpec secretKeySpec = new SecretKeySpec(
				jwtProperties.secret().getBytes(StandardCharsets.UTF_8),
				HMAC_SHA256
			);
			mac.init(secretKeySpec);
			byte[] signature = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
		} catch (java.security.GeneralSecurityException exception) {
			throw new IllegalStateException("Failed to sign JWT.", exception);
		}
	}
}
