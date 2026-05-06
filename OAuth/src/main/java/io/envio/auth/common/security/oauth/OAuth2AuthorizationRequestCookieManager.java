package io.envio.auth.common.security.oauth;

import java.time.Duration;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthorizationRequestCookieManager {

	private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";

	public String getRequestId(final HttpServletRequest request) {
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

	public void addCookie(final HttpServletResponse response, final String requestId, final Duration ttl) {
		Cookie cookie = createCookie(requestId);
		cookie.setMaxAge((int)ttl.toSeconds());
		response.addCookie(cookie);
	}

	public void deleteCookie(final HttpServletResponse response) {
		Cookie cookie = createCookie("");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	private Cookie createCookie(final String value) {
		Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, value);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		return cookie;
	}
}
