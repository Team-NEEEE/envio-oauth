package io.envio.auth.common.security.oauth;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieOAuth2AuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	private static final String OAUTH2_AUTHORIZATION_REQUEST_REDIS_KEY_PREFIX = "oauth2:authorization-request:";
	private static final Duration AUTHORIZATION_REQUEST_TTL = Duration.ofSeconds(180);

	private final StringRedisTemplate stringRedisTemplate;
	private final ObjectMapper objectMapper;

	public CookieOAuth2AuthorizationRequestRepository(
		final StringRedisTemplate stringRedisTemplate,
		final ObjectMapper objectMapper
	) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(final HttpServletRequest request) {
		return getAuthorizationRequest(request);
	}

	@Override
	public void saveAuthorizationRequest(
		final OAuth2AuthorizationRequest authorizationRequest,
		final HttpServletRequest request,
		final HttpServletResponse response
	) {
		if (authorizationRequest == null) {
			deleteAuthorizationRequest(request);
			deleteCookie(response);
			return;
		}

		String requestId = UUID.randomUUID().toString();
		String redisKey = createRedisKey(requestId);
		stringRedisTemplate.opsForValue()
			.set(redisKey, serialize(authorizationRequest), AUTHORIZATION_REQUEST_TTL);
		addCookie(response, requestId);
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(
		final HttpServletRequest request,
		final HttpServletResponse response
	) {
		OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
		deleteAuthorizationRequest(request);
		deleteCookie(response);
		return authorizationRequest;
	}

	private OAuth2AuthorizationRequest getAuthorizationRequest(final HttpServletRequest request) {
		String requestId = getRequestId(request);
		if (requestId == null) {
			return null;
		}

		String authorizationRequestJson = stringRedisTemplate.opsForValue().get(createRedisKey(requestId));
		if (authorizationRequestJson == null) {
			return null;
		}

		return deserialize(authorizationRequestJson);
	}

	private void deleteAuthorizationRequest(final HttpServletRequest request) {
		String requestId = getRequestId(request);
		if (requestId != null) {
			stringRedisTemplate.delete(createRedisKey(requestId));
		}
	}

	private String getRequestId(final HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}

		for (final Cookie cookie : request.getCookies()) {
			if (OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}

		return null;
	}

	private String serialize(final OAuth2AuthorizationRequest authorizationRequest) {
		AuthorizationRequestPayload payload = AuthorizationRequestPayload.from(authorizationRequest);
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize OAuth2 authorization request.", exception);
		}
	}

	private OAuth2AuthorizationRequest deserialize(final String authorizationRequestJson) {
		try {
			return objectMapper.readValue(authorizationRequestJson, AuthorizationRequestPayload.class)
				.toAuthorizationRequest();
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to deserialize OAuth2 authorization request.", exception);
		}
	}

	private void addCookie(final HttpServletResponse response, final String requestId) {
		Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, requestId);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge((int)AUTHORIZATION_REQUEST_TTL.toSeconds());
		response.addCookie(cookie);
	}

	private void deleteCookie(final HttpServletResponse response) {
		Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, "");
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	private String createRedisKey(final String requestId) {
		return OAUTH2_AUTHORIZATION_REQUEST_REDIS_KEY_PREFIX + requestId;
	}

	private record AuthorizationRequestPayload(
		String authorizationUri,
		String clientId,
		String redirectUri,
		Set<String> scopes,
		String state,
		Map<String, Object> additionalParameters,
		String authorizationRequestUri,
		Map<String, Object> attributes
	) {

		private static AuthorizationRequestPayload from(final OAuth2AuthorizationRequest authorizationRequest) {
			return new AuthorizationRequestPayload(
				authorizationRequest.getAuthorizationUri(),
				authorizationRequest.getClientId(),
				authorizationRequest.getRedirectUri(),
				authorizationRequest.getScopes(),
				authorizationRequest.getState(),
				authorizationRequest.getAdditionalParameters(),
				authorizationRequest.getAuthorizationRequestUri(),
				authorizationRequest.getAttributes()
			);
		}

		private OAuth2AuthorizationRequest toAuthorizationRequest() {
			return OAuth2AuthorizationRequest.authorizationCode()
				.authorizationUri(authorizationUri)
				.clientId(clientId)
				.redirectUri(redirectUri)
				.scopes(scopes)
				.state(state)
				.additionalParameters(additionalParameters)
				.authorizationRequestUri(authorizationRequestUri)
				.attributes(attributes)
				.build();
		}
	}
}
