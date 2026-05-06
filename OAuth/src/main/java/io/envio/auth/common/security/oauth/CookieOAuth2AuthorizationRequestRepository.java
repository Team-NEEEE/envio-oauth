package io.envio.auth.common.security.oauth;

import java.time.Duration;
import java.util.UUID;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CookieOAuth2AuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private static final Duration AUTHORIZATION_REQUEST_TTL = Duration.ofSeconds(180);

	private final AuthorizationRequestRedisStore authorizationRequestRedisStore;
	private final OAuth2AuthorizationRequestCookieManager cookieManager;

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(final HttpServletRequest request) {
		String requestId = cookieManager.getRequestId(request);
		if (requestId == null) {
			return null;
		}

		return authorizationRequestRedisStore.find(requestId).orElse(null);
	}

	@Override
	public void saveAuthorizationRequest(
		final OAuth2AuthorizationRequest authorizationRequest,
		final HttpServletRequest request,
		final HttpServletResponse response
	) {
		if (authorizationRequest == null) {
			deleteAuthorizationRequest(request, response);
			return;
		}

		String requestId = UUID.randomUUID().toString();
		authorizationRequestRedisStore.save(requestId, authorizationRequest, AUTHORIZATION_REQUEST_TTL);
		cookieManager.addCookie(response, requestId, AUTHORIZATION_REQUEST_TTL);
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(
		final HttpServletRequest request,
		final HttpServletResponse response
	) {
		OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
		deleteAuthorizationRequest(request, response);
		return authorizationRequest;
	}

	private void deleteAuthorizationRequest(final HttpServletRequest request, final HttpServletResponse response) {
		String requestId = cookieManager.getRequestId(request);
		if (requestId != null) {
			authorizationRequestRedisStore.delete(requestId);
		}
		cookieManager.deleteCookie(response);
	}
}
