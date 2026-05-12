package io.envio.auth.domain.view.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record AuthMeResDto(
	@JsonProperty("user_id")
	Long userId,

	@JsonProperty("github_id")
	String githubId,

	String email,

	String role,

	@JsonProperty("public_key")
	String publicKey
) {
}
