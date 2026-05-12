package io.envio.auth.common.security.jwt;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.common.response.BaseResponse;
import io.envio.auth.common.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private static final String FAILURE_MESSAGE = "인증에 실패했습니다.";

	private final ObjectMapper objectMapper;

	@Override
	public void commence(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final AuthenticationException authException
	) throws IOException {
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.UNAUTHORIZED, request);

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), BaseResponse.fail(FAILURE_MESSAGE, errorResponse));
	}
}
