package io.envio.auth.domain.view.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.common.error.exception.BusinessException;
import io.envio.auth.common.error.exception.handler.GlobalExceptionHandler;
import io.envio.auth.common.security.jwt.JwtClaims;
import io.envio.auth.domain.user.entity.UserRole;
import io.envio.auth.domain.view.dto.request.AuthProjectMemberRoleUpdateReqDto;
import io.envio.auth.domain.view.dto.request.AuthRefreshReqDto;
import io.envio.auth.domain.view.dto.response.AuthProjectMemberRoleUpdateResDto;
import io.envio.auth.domain.view.dto.response.AuthRefreshResDto;
import io.envio.auth.domain.view.service.ViewAuthService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ViewAuthController")
class ViewAuthControllerTest {

	@Mock
	private ViewAuthService viewAuthService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper().findAndRegisterModules();

		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		mockMvc = MockMvcBuilders.standaloneSetup(new ViewAuthController(viewAuthService))
			.setControllerAdvice(new GlobalExceptionHandler())
			.setCustomArgumentResolvers(new JwtClaimsArgumentResolver())
			.setValidator(validator)
			.build();
	}

	@Test
	@DisplayName("refreshToken 재발급 요청이 성공하면 200 응답과 새 토큰을 반환한다")
	void refreshTokenReturnsNewTokens() throws Exception {
		// given
		final AuthRefreshReqDto reqDto = new AuthRefreshReqDto("refresh-token");
		when(viewAuthService.refreshToken(reqDto))
			.thenReturn(new AuthRefreshResDto("new-access-token", "new-refresh-token"));

		// when & then
		mockMvc.perform(post("/api/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reqDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("토큰이 재발급되었습니다."))
			.andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
			.andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
	}

	@Test
	@DisplayName("refreshToken이 비어 있으면 400 응답을 반환한다")
	void refreshTokenReturnsBadRequestWhenRefreshTokenIsBlank() throws Exception {
		// given
		final AuthRefreshReqDto reqDto = new AuthRefreshReqDto("");

		// when & then
		mockMvc.perform(post("/api/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reqDto)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errors[0].message").value("refreshToken은 필수입니다."));
	}

	@Test
	@DisplayName("서비스에서 인증 예외가 발생하면 401 응답을 반환한다")
	void refreshTokenReturnsUnauthorizedWhenServiceThrowsBusinessException() throws Exception {
		// given
		final AuthRefreshReqDto reqDto = new AuthRefreshReqDto("refresh-token");
		when(viewAuthService.refreshToken(reqDto))
			.thenThrow(new BusinessException(ErrorCode.UNAUTHORIZED));

		// when & then
		mockMvc.perform(post("/api/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reqDto)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.message", containsString("Authentication")));
	}

	@Test
	@DisplayName("로그아웃 요청이 성공하면 200 응답을 반환한다")
	void logoutReturnsOk() throws Exception {
		// given
		final JwtClaims claims = new JwtClaims(1L, "123456", "user@example.com", "VIEWER");

		// when & then
		mockMvc.perform(post("/api/auth/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.requestAttr("claims", claims))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."));

		verify(viewAuthService).logout(claims);
	}

	@Test
	@DisplayName("인증 정보 없이 로그아웃을 요청하면 401 응답을 반환한다")
	void logoutReturnsUnauthorizedWhenClaimsIsMissing() throws Exception {
		// when & then
		mockMvc.perform(post("/api/auth/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("프로젝트 멤버 역할 변경 요청이 성공하면 200 응답을 반환한다")
	void updateProjectMemberRoleReturnsOk() throws Exception {
		// given
		final JwtClaims claims = new JwtClaims(1L, "123456", "owner@example.com", "OWNER");
		final AuthProjectMemberRoleUpdateReqDto reqDto = new AuthProjectMemberRoleUpdateReqDto(UserRole.ADMIN);
		final AuthProjectMemberRoleUpdateResDto resDto = AuthProjectMemberRoleUpdateResDto.builder()
			.projectId(10L)
			.userId(3L)
			.role(UserRole.ADMIN)
			.updatedAt(null)
			.build();
		when(viewAuthService.updateProjectMemberRole(claims, 10L, 3L, reqDto)).thenReturn(resDto);

		// when & then
		mockMvc.perform(patch("/api/auth/projects/{projectId}/members/{userId}/role", 10L, 3L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reqDto))
				.requestAttr("claims", claims))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("멤버 역할이 변경되었습니다."))
			.andExpect(jsonPath("$.data.project_id").value(10L))
			.andExpect(jsonPath("$.data.user_id").value(3L))
			.andExpect(jsonPath("$.data.role").value("ADMIN"));

		verify(viewAuthService).updateProjectMemberRole(claims, 10L, 3L, reqDto);
	}

	@Test
	@DisplayName("인증 정보 없이 프로젝트 멤버 역할 변경을 요청하면 401 응답을 반환한다")
	void updateProjectMemberRoleReturnsUnauthorizedWhenClaimsIsMissing() throws Exception {
		// given
		final AuthProjectMemberRoleUpdateReqDto reqDto = new AuthProjectMemberRoleUpdateReqDto(UserRole.ADMIN);

		// when & then
		mockMvc.perform(patch("/api/auth/projects/{projectId}/members/{userId}/role", 10L, 3L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reqDto)))
			.andExpect(status().isUnauthorized());
	}

	private static class JwtClaimsArgumentResolver implements HandlerMethodArgumentResolver {

		@Override
		public boolean supportsParameter(final MethodParameter parameter) {
			return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
				&& JwtClaims.class.isAssignableFrom(parameter.getParameterType());
		}

		@Override
		@Nullable
		public Object resolveArgument(
			final MethodParameter parameter,
			@Nullable final ModelAndViewContainer mavContainer,
			final NativeWebRequest webRequest,
			@Nullable final WebDataBinderFactory binderFactory
		) {
			return webRequest.getAttribute("claims", NativeWebRequest.SCOPE_REQUEST);
		}
	}
}
