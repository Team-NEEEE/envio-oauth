package io.envio.auth.common.security.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.common.response.BaseResponse;
import io.envio.auth.common.response.ErrorResponse;
import io.envio.auth.common.security.SecurityConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String FAILURE_MESSAGE = "인증에 실패했습니다.";
	private static final RequestMatcher[] EXCLUDE_REQUEST_MATCHERS = createExcludeRequestMatchers();

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;

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

		String token = authorizationHeader.substring(BEARER_PREFIX.length());
		try {
			JwtClaims claims = jwtTokenProvider.parseAccessToken(token);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				claims,
				null,
				List.of(new SimpleGrantedAuthority("ROLE_" + claims.role()))
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (IllegalArgumentException exception) {
			SecurityContextHolder.clearContext();
			log.warn("Invalid JWT access token", exception);
			writeUnauthorizedResponse(request, response);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private void writeUnauthorizedResponse(
		final HttpServletRequest request,
		final HttpServletResponse response
	) throws IOException {
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.UNAUTHORIZED, request);

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), BaseResponse.fail(FAILURE_MESSAGE, errorResponse));
	}
}
