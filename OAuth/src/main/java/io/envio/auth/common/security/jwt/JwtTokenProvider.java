package io.envio.auth.common.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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

	public JwtClaims parseAccessToken(final String token) {
		return parseToken(token, TOKEN_TYPE_ACCESS);
	}

	public JwtClaims parseRefreshToken(final String token) {
		return parseToken(token, TOKEN_TYPE_REFRESH);
	}

	private JwtClaims parseToken(final String token, final String expectedTokenType) {
		String[] tokenParts = token.split("\\.");
		if (tokenParts.length != 3) {
			throw new JwtParsingException("Invalid JWT format.");
		}

		validateSignature(tokenParts);
		JsonNode header = decodeJson(tokenParts[0]);
		validateHeader(header);

		JsonNode payload = decodeJson(tokenParts[1]);
		validateExpiration(payload);
		validateTokenType(payload, expectedTokenType);

		return new JwtClaims(
			getRequiredLongClaim(payload, "userId"),
			getRequiredTextClaim(payload, "githubId"),
			getRequiredTextClaim(payload, "email"),
			getRequiredTextClaim(payload, "role")
		);
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

	private JsonNode decodeJson(final String value) {
		try {
			byte[] decoded = Base64.getUrlDecoder().decode(value);
			return objectMapper.readTree(decoded);
		} catch (IllegalArgumentException | java.io.IOException exception) {
			throw new JwtParsingException("Failed to decode JWT.", exception);
		}
	}

	private void validateHeader(final JsonNode header) {
		if (!"HS256".equals(header.path("alg").asText()) || !"JWT".equals(header.path("typ").asText())) {
			throw new JwtParsingException("Invalid JWT header.");
		}
	}

	private void validateSignature(final String[] tokenParts) {
		byte[] expectedBytes = signRaw(tokenParts[0] + "." + tokenParts[1]);
		byte[] actualBytes = decodeSignature(tokenParts[2]);
		boolean valid = MessageDigest.isEqual(expectedBytes, actualBytes);
		if (!valid) {
			throw new JwtParsingException("Invalid JWT signature.");
		}
	}

	private void validateExpiration(final JsonNode payload) {
		long expiration = payload.path("exp").asLong(0);
		if (expiration <= Instant.now().getEpochSecond()) {
			throw new JwtParsingException("Expired JWT.");
		}
	}

	private void validateTokenType(final JsonNode payload, final String expectedTokenType) {
		if (!expectedTokenType.equals(payload.path("tokenType").asText())) {
			throw new JwtParsingException("Invalid JWT token type.");
		}
	}

	private Long getRequiredLongClaim(final JsonNode payload, final String claimName) {
		JsonNode claim = payload.path(claimName);
		if (claim.isMissingNode() || !claim.canConvertToLong()) {
			throw new JwtParsingException(claimName + " claim is missing from JWT.");
		}
		return claim.asLong();
	}

	private String getRequiredTextClaim(final JsonNode payload, final String claimName) {
		JsonNode claim = payload.path(claimName);
		if (claim.isMissingNode() || !claim.isTextual() || claim.asText().isBlank()) {
			throw new JwtParsingException(claimName + " claim is missing or blank in JWT.");
		}
		return claim.asText();
	}

	private String sign(final String value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(signRaw(value));
	}

	private byte[] signRaw(final String value) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			SecretKeySpec secretKeySpec = new SecretKeySpec(
				jwtProperties.secret().getBytes(StandardCharsets.UTF_8),
				HMAC_SHA256
			);
			mac.init(secretKeySpec);
			return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
		} catch (java.security.GeneralSecurityException exception) {
			throw new IllegalStateException("Failed to sign JWT.", exception);
		}
	}

	private byte[] decodeSignature(final String signature) {
		try {
			return Base64.getUrlDecoder().decode(signature);
		} catch (IllegalArgumentException exception) {
			throw new JwtParsingException("Invalid JWT signature encoding.", exception);
		}
	}
}
