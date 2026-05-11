package io.envio.auth.domain.cli.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.envio.auth.common.response.BaseResponse;
import io.envio.auth.common.util.ResponseUtils;
import io.envio.auth.domain.cli.dto.request.CliLoginSaveReqDto;
import io.envio.auth.domain.cli.dto.request.CliLoginStatusReqDto;
import io.envio.auth.domain.cli.dto.response.CliLoginSaveResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStartResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStatusResDto;
import io.envio.auth.domain.cli.service.facade.CliAuthFacadeService;
import io.envio.auth.domain.cli.view.CliAuthRedirectView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "CLI Auth", description = "CLI GitHub OAuth login API")
@RestController
@RequestMapping("/api/auth/cli/login")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliAuthController {

	private final CliAuthFacadeService facadeService;
	private final CliAuthRedirectView redirectView;

	@Operation(
		summary = "CLI login start",
		description = "Create a temporary CLI login session and return a GitHub OAuth URL."
	)
	@PostMapping("/start")
	public ResponseEntity<BaseResponse<CliLoginStartResDto>> startLogin() {
		CliLoginStartResDto response = facadeService.startLogin();
		return ResponseUtils.ok(response);
	}

	@Operation(
		summary = "GitHub OAuth redirect",
		description = "Handle the browser OAuth callback and mark the CLI login session as authenticated."
	)
	@GetMapping(value = "/github/redirect", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> handleGithubRedirect(
		@RequestParam("code") final String code,
		@RequestParam("state") final String state
	) {
		facadeService.processGithubCallback(code, state);
		return ResponseEntity.ok()
			.contentType(MediaType.TEXT_HTML)
			.body(redirectView.success());
	}

	@Operation(
		summary = "CLI login status",
		description = "Return the current authentication status of a CLI login session."
	)
	@GetMapping("/github/callback")
	public ResponseEntity<BaseResponse<CliLoginStatusResDto>> getLoginStatus(
		@RequestParam final String loginSessionId
	) {
		CliLoginStatusResDto response = facadeService.getLoginStatus(new CliLoginStatusReqDto(loginSessionId));
		return ResponseUtils.ok(response);
	}

	@Operation(
		summary = "CLI login save",
		description = "Register CLI device public key after OAuth login succeeds, then remove the temporary session."
	)
	@PostMapping("/save")
	public ResponseEntity<BaseResponse<CliLoginSaveResDto>> saveCliUser(
		@Valid @RequestBody final CliLoginSaveReqDto reqDto
	) {
		CliLoginSaveResDto response = facadeService.saveCliUser(reqDto);
		return ResponseUtils.ok(response);
	}
}
