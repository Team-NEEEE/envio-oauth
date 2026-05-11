package io.envio.auth.domain.cli.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "CLI login status request")
public record CliLoginStatusReqDto(
	@Schema(description = "CLI login session ID", example = "550e8400-e29b-41d4-a716-446655440000")
	@NotBlank(message = "Login session ID is required.")
	String loginSessionId
) {
}
