package io.envio.auth.common.security.jwt;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.envio.auth.common.security.SecurityConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final RequestMatcher[] EXCLUDE_REQUEST_MATCHERS = createExcludeRequestMatchers();

	private static RequestMatcher[] createExcludeRequestMatchers() {
		RequestMatcher[] requestMatchers = new RequestMatcher[SecurityConstants.JWT_EXCLUDE_URLS.length];
		for (int i = 0; i < SecurityConstants.JWT_EXCLUDE_URLS.length; i++) {
			requestMatchers[i] = PathPatternRequestMatcher.pathPattern(SecurityConstants.JWT_EXCLUDE_URLS[i]);
		}
		return requestMatchers;
	}

	@Override
	protected boolean shouldNotFilter(final HttpServletRequest request) {
		for (final RequestMatcher requestMatcher : EXCLUDE_REQUEST_MATCHERS) {
			if (requestMatcher.matches(request)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected void doFilterInternal(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final FilterChain filterChain
	) throws ServletException, IOException {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		filterChain.doFilter(request, response);
	}
}
