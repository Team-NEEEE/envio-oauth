package io.envio.auth.domain.cli.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "CLI login save response")
@Builder
public record CliLoginSaveResDto(
	@Schema(description = "GitHub user ID", example = "123456")
	String githubId,

	@Schema(description = "GitHub email resolved from the authenticated session", example = "mingi.lee@example.com")
	String email
) {
}
