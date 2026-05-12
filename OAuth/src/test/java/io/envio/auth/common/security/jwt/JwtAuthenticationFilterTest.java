package io.envio.auth.common.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint(
		objectMapper
	);

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("유효한 Bearer 토큰이면 SecurityContext에 인증 정보를 등록한다")
	void doFilterInternalSetsAuthenticationWhenTokenIsValid() throws ServletException, IOException {
		// given
		final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
			jwtTokenProvider,
			jwtAuthenticationEntryPoint
		);
		final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final MockFilterChain filterChain = new MockFilterChain();
		final JwtClaims claims = new JwtClaims(1L, "123456", "user@example.com", "VIEWER");
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer access-token");
		when(jwtTokenProvider.parseAccessToken("access-token")).thenReturn(claims);

		// when
		filter.doFilter(request, response, filterChain);

		// then
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		assertEquals(claims, authentication.getPrincipal());
		assertEquals("ROLE_VIEWER", authentication.getAuthorities().iterator().next().getAuthority());
		verify(jwtTokenProvider).parseAccessToken("access-token");
	}

	@Test
	@DisplayName("Authorization 헤더가 없으면 SecurityContext에 인증 정보를 등록하지 않는다")
	void doFilterInternalDoesNotSetAuthenticationWhenAuthorizationHeaderIsMissing()
		throws ServletException, IOException {
		// given
		final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
			jwtTokenProvider,
			jwtAuthenticationEntryPoint
		);
		final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final MockFilterChain filterChain = new MockFilterChain();

		// when
		filter.doFilter(request, response, filterChain);

		// then
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	@DisplayName("유효하지 않은 Bearer 토큰이면 401 응답을 반환한다")
	void doFilterInternalWritesUnauthorizedResponseWhenTokenIsInvalid() throws ServletException, IOException {
		// given
		final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
			jwtTokenProvider,
			jwtAuthenticationEntryPoint
		);
		final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final MockFilterChain filterChain = new MockFilterChain();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
		doThrow(new JwtParsingException("Invalid JWT."))
			.when(jwtTokenProvider)
			.parseAccessToken("invalid-token");

		// when
		filter.doFilter(request, response, filterChain);

		// then
		final JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
		assertEquals(MockHttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
		assertEquals("인증에 실패했습니다.", responseBody.get("message").asText());
		assertEquals("Authentication is required.", responseBody.get("error").get("message").asText());
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}
}
