package io.envio.auth.common.security.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final List<String> EXCLUDE_URLS = List.of(
		"/api/auth/oauth/**",
		"/api/auth/refresh"
	);

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	protected boolean shouldNotFilter(final HttpServletRequest request) {
		String requestUri = request.getRequestURI();
		return EXCLUDE_URLS.stream()
			.anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
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
