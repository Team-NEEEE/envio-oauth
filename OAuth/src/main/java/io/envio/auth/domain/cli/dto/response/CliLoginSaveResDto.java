package io.envio.auth.domain.cli.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "CLI login save response")
@Builder
public record CliLoginSaveResDto(
	@JsonProperty("user_id")
	@Schema(description = "Created or existing user ID", example = "105")
	Long userId,

	@JsonProperty("device_id")
	@Schema(description = "Registered CLI device ID", example = "42")
	Long deviceId,

	@Schema(description = "GitHub login", example = "mingi-lee")
	String githubId,

	@Schema(description = "GitHub email resolved from the authenticated session", example = "mingi.lee@example.com")
	String email
) {
}
