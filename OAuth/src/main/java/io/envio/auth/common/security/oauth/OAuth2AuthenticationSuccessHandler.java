package io.envio.auth.common.security.oauth;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.envio.auth.common.response.BaseResponse;
import io.envio.auth.domain.view.dto.response.OAuthLoginResDto;
import io.envio.auth.domain.view.service.ViewAuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private static final String SUCCESS_MESSAGE = "GitHub 로그인이 완료되었습니다.";

	private final ViewAuthService viewAuthService;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final Authentication authentication
	) throws IOException, ServletException {
		OAuthLoginResDto responseBody = viewAuthService.issueOAuthLoginTokens(authentication);

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), BaseResponse.ok(SUCCESS_MESSAGE, responseBody));
	}
}
