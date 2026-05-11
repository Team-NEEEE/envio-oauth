package io.envio.auth.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.envio.auth.common.response.BaseResponse;
import io.envio.auth.common.util.ResponseUtils;
import io.envio.auth.domain.user.dto.request.UserCreateReqDto;
import io.envio.auth.domain.user.dto.response.UserResDto;
import io.envio.auth.domain.user.service.facade.UserFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserController {

	private final UserFacadeService facadeService;

	@Operation(summary = "사용자 생성", description = "GitHub 사용자 정보를 기반으로 사용자를 생성합니다.")
	@PostMapping
	public ResponseEntity<BaseResponse<UserResDto>> createUser(
		@Valid @RequestBody final UserCreateReqDto reqDto
	) {
		UserResDto response = facadeService.createUser(reqDto);
		return ResponseUtils.created(response);
	}

	@Operation(summary = "사용자 조회", description = "사용자 ID로 사용자를 조회합니다.")
	@GetMapping("/{userId}")
	public ResponseEntity<BaseResponse<UserResDto>> getUser(
		@PathVariable final Long userId
	) {
		UserResDto response = facadeService.getUser(userId);
		return ResponseUtils.ok(response);
	}

	@Operation(summary = "GitHub ID 기반 사용자 조회", description = "GitHub ID로 사용자를 조회합니다.")
	@GetMapping
	public ResponseEntity<BaseResponse<UserResDto>> getUserByGithubId(
		@RequestParam(required = true) final String githubId
	) {
		UserResDto response = facadeService.getUserByGithubId(githubId);
		return ResponseUtils.ok(response);
	}
}
