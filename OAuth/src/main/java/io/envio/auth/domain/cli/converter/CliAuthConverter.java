package io.envio.auth.domain.cli.converter;

import io.envio.auth.domain.cli.dto.request.CliLoginSaveReqDto;
import io.envio.auth.domain.cli.dto.response.CliLoginSaveResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStartResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStatusResDto;
import io.envio.auth.domain.cli.entity.RedisCliSession;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserDevice;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CliAuthConverter {

	public CliLoginStartResDto toLoginStartResDto(final String loginSessionId, final String loginUrl) {
		return CliLoginStartResDto.builder()
			.loginSessionId(loginSessionId)
			.loginUrl(loginUrl)
			.build();
	}

	public CliLoginStatusResDto toLoginStatusResDto(final RedisCliSession session) {
		return CliLoginStatusResDto.builder()
			.status(session.getStatus())
			.githubId(session.getGithubId())
			.email(session.getEmail())
			.build();
	}

	public User toUser(final RedisCliSession session) {
		return User.createGithubUser(session.getGithubId(), resolveEmail(session));
	}

	public UserDevice toUserDevice(final CliLoginSaveReqDto reqDto, final User user) {
		return UserDevice.builder()
			.user(user)
			.deviceName(reqDto.deviceName())
			.publicKey(reqDto.publicKey())
			.build();
	}

	public CliLoginSaveResDto toLoginSaveResDto(final User user, final UserDevice userDevice) {
		return CliLoginSaveResDto.builder()
			.userId(user.getId())
			.deviceId(userDevice.getId())
			.githubId(user.getGithubId())
			.email(user.getEmail())
			.build();
	}

	private String resolveEmail(final RedisCliSession session) {
		if (session.getEmail() != null && !session.getEmail().isBlank()) {
			return session.getEmail();
		}

		return session.getGithubId() + "@users.noreply.github.com";
	}
}
