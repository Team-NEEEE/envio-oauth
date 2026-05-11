package io.envio.auth.domain.cli.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("cli_session")
public class RedisCliSession {

	public static final String STATUS_PENDING = "PENDING";
	public static final String STATUS_SUCCESS = "SUCCESS";

	@Id
	private String id;

	private String status;

	private String githubId;

	private String email;

	@TimeToLive
	private Integer expiresIn;

	public void completeAuth(final String githubId, final String email) {
		this.status = STATUS_SUCCESS;
		this.githubId = githubId;
		this.email = email;
	}
}
