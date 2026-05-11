package io.envio.auth.domain.cli.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Schema(description = "CLI login save request")
@Builder
public record CliLoginSaveReqDto(
	@Schema(description = "Authenticated CLI login session ID", example = "550e8400-e29b-41d4-a716-446655440000")
	@NotBlank(message = "Login session ID is required.")
	String loginSessionId,

	@Schema(description = "GitHub user ID verified by the login session", example = "123456")
	@NotBlank(message = "GitHub ID is required.")
	String githubId,

	@Schema(description = "CLI device name", example = "Mingi-MacBook-Pro")
	@NotBlank(message = "Device name is required.")
	@Size(max = 255, message = "Device name must be 255 characters or fewer.")
	String deviceName,

	@Schema(description = "SSH public key used by the CLI", example = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAI...")
	@NotBlank(message = "Public key is required.")
	@Pattern(
		regexp = "^(ssh-rsa|ssh-ed25519|ecdsa-sha2-nistp(256|384|521))\\s+[A-Za-z0-9+/=]+(?:\\s+.*)?$",
		message = "Public key must be a valid SSH public key."
	)
	String publicKey
) {
}
