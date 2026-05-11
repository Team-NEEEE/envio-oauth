package io.envio.auth.common.security.oauth;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.common.response.BaseResponse;
import io.envio.auth.common.response.ErrorResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

	private static final String FAILURE_MESSAGE = "인증에 실패했습니다.";

	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationFailure(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final AuthenticationException exception
	) throws IOException, ServletException {
		log.warn("OAuth2 authentication failed", exception);

		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.UNAUTHORIZED, request);

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), BaseResponse.fail(FAILURE_MESSAGE, errorResponse));
	}
}
