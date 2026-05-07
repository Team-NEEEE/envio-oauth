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
@RedisHash("cli_session") // Redis에 저장될 때 Key의 Prefix (예: cli_session:550e8400...)
public class RedisCliSession {

	// 주의: jakarta.persistence.Id 가 아니라 org.springframework.data.annotation.Id 입니다!
	@Id
	private String id; // loginSessionId

	private String status; // PENDING, SUCCESS

	private String githubId;

	private String email;

	@TimeToLive // Redis의 만료 시간(TTL)을 자동으로 관리해주는 마법의 어노테이션
	private Integer expiresIn; // 초 단위

	// 비즈니스 로직 (서버가 GitHub 콜백을 받았을 때 상태를 업데이트하기 위함)
	public void completeAuth(String githubId, String email) {
		this.status = "SUCCESS";
		this.githubId = githubId;
		this.email = email;
	}
}