package io.envio.auth.common.security.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.envio.auth.domain.view.dto.response.OAuthLoginResDto;
import io.envio.auth.domain.view.service.ViewAuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2AuthenticationSuccessHandler")
class OAuth2AuthenticationSuccessHandlerTest {

	@Mock
	private ViewAuthService viewAuthService;

	@Mock
	private Authentication authentication;

	private ObjectMapper objectMapper;
	private OAuth2AuthenticationSuccessHandler successHandler;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules();
		successHandler = new OAuth2AuthenticationSuccessHandler(viewAuthService, objectMapper);
	}

	@Test
	@DisplayName("OAuth 인증 성공 시 200 상태와 로그인 완료 응답을 반환한다")
	void onAuthenticationSuccessWritesLoginResponse() throws ServletException, IOException {
		// given
		final OAuthLoginResDto loginResponse = OAuthLoginResDto.builder()
			.accessToken("access-token")
			.refreshToken("refresh-token")
			.userId(1L)
			.email("user@example.com")
			.role("VIEWER")
			.build();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		when(viewAuthService.issueOAuthLoginTokens(authentication)).thenReturn(loginResponse);

		// when
		successHandler.onAuthenticationSuccess(request, response, authentication);

		// then
		final JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		assertTrue(response.getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));
		assertEquals("UTF-8", response.getCharacterEncoding());
		assertEquals("GitHub 로그인이 완료되었습니다.", responseBody.get("message").asText());
		assertEquals("access-token", responseBody.get("data").get("accessToken").asText());
		assertEquals("refresh-token", responseBody.get("data").get("refreshToken").asText());
		assertEquals(1L, responseBody.get("data").get("userId").asLong());
		assertEquals("user@example.com", responseBody.get("data").get("email").asText());
		assertEquals("VIEWER", responseBody.get("data").get("role").asText());
		verify(viewAuthService).issueOAuthLoginTokens(authentication);
	}
}
