package io.envio.auth.domain.view.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.envio.auth.common.response.BaseResponse;
import io.envio.auth.common.security.jwt.JwtClaims;
import io.envio.auth.domain.view.dto.request.AuthRefreshReqDto;
import io.envio.auth.domain.view.dto.response.AuthMeResDto;
import io.envio.auth.domain.view.dto.response.AuthRefreshResDto;
import io.envio.auth.domain.view.service.ViewAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ViewAuthController {

	private static final String ME_SUCCESS_MESSAGE = "사용자 정보 조회에 성공했습니다.";
	private static final String REFRESH_SUCCESS_MESSAGE = "토큰이 재발급되었습니다.";

	private final ViewAuthService viewAuthService;

	@GetMapping("/me")
	public ResponseEntity<BaseResponse<AuthMeResDto>> getCurrentUser(
		@AuthenticationPrincipal final JwtClaims claims
	) {
		AuthMeResDto response = viewAuthService.getCurrentUser(claims);
		return ResponseEntity.ok(BaseResponse.ok(ME_SUCCESS_MESSAGE, response));
	}

	@PostMapping("/refresh")
	public ResponseEntity<BaseResponse<AuthRefreshResDto>> refreshToken(
		@Valid @RequestBody final AuthRefreshReqDto reqDto
	) {
		AuthRefreshResDto response = viewAuthService.refreshToken(reqDto);
		return ResponseEntity.ok(BaseResponse.ok(REFRESH_SUCCESS_MESSAGE, response));
	}
}
