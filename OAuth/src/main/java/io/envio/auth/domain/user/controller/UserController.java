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

@Tag(name = "사용자 (User)", description = "사용자 도메인 예제 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserController {

	private final UserFacadeService facadeService;

	@Operation(summary = "사용자 생성", description = "요청 DTO를 검증한 뒤 command 서비스로 사용자 생성을 위임합니다.")
	@PostMapping
	public ResponseEntity<BaseResponse<UserResDto>> createUser(
		@Valid @RequestBody final UserCreateReqDto reqDto
	) {
		UserResDto response = facadeService.createUser(reqDto);
		return ResponseUtils.created(response);
	}

	@Operation(summary = "사용자 단건 조회", description = "PathVariable로 받은 사용자 ID를 query 서비스 조회 흐름에 위임합니다.")
	@GetMapping("/{userId}")
	public ResponseEntity<BaseResponse<UserResDto>> getUser(
		@PathVariable final Long userId
	) {
		UserResDto response = facadeService.getUser(userId);
		return ResponseUtils.ok(response);
	}

	@Operation(summary = "사번 기반 사용자 조회", description = "QueryParameter로 받은 사번을 query 서비스 조회 흐름에 위임합니다.")
	@GetMapping(params = "employeeNumber")
	public ResponseEntity<BaseResponse<UserResDto>> getUserByEmployeeNumber(
		@RequestParam final String employeeNumber
	) {
		UserResDto response = facadeService.getUserByEmployeeNumber(employeeNumber);
		return ResponseUtils.ok(response);
	}
}
