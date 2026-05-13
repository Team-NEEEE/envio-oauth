package io.envio.auth.domain.cli.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.envio.auth.domain.cli.dto.request.CliLoginSaveReqDto;
import io.envio.auth.domain.cli.dto.response.CliLoginSaveResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStartResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStatusResDto;
import io.envio.auth.domain.cli.entity.RedisCliSession;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserDevice;
import io.envio.auth.domain.user.entity.UserRole;

@DisplayName("CLI Auth Converter")
class CliAuthConverterTest {

	@Test
	@DisplayName("login session id and url are mapped to start response")
	void toLoginStartResDtoMapsSessionIdAndLoginUrl() {
		// when
		CliLoginStartResDto resDto = CliAuthConverter.toLoginStartResDto("session-id", "https://github.com/login");

		// then
		assertEquals("session-id", resDto.loginSessionId());
		assertEquals("https://github.com/login", resDto.loginUrl());
	}

	@Test
	@DisplayName("Redis CLI session is mapped to status response")
	void toLoginStatusResDtoMapsSession() {
		// given
		RedisCliSession session = RedisCliSession.builder()
			.id("session-id")
			.status(RedisCliSession.STATUS_SUCCESS)
			.githubId("123456")
			.email("user@example.com")
			.expiresIn(300)
			.build();

		// when
		CliLoginStatusResDto resDto = CliAuthConverter.toLoginStatusResDto(session);

		// then
		assertEquals(RedisCliSession.STATUS_SUCCESS, resDto.status());
		assertEquals("123456", resDto.githubId());
		assertEquals("user@example.com", resDto.email());
	}

	@Test
	@DisplayName("Redis CLI session is mapped to GitHub user")
	void toUserMapsSession() {
		// given
		RedisCliSession session = RedisCliSession.builder()
			.githubId("123456")
			.email("user@example.com")
			.build();

		// when
		User user = CliAuthConverter.toUser(session);

		// then
		assertEquals("123456", user.getGithubId());
		assertEquals("user@example.com", user.getEmail());
		assertEquals(UserRole.VIEWER, user.getRole());
	}

	@Test
	@DisplayName("save request and user are mapped to user device")
	void toUserDeviceMapsRequestAndUser() {
		// given
		User user = createUser();
		CliLoginSaveReqDto reqDto = saveRequest("Laptop");

		// when
		UserDevice userDevice = CliAuthConverter.toUserDevice(reqDto, user);

		// then
		assertEquals(user, userDevice.getUser());
		assertEquals("Laptop", userDevice.getDeviceName());
		assertEquals("ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAITestKey", userDevice.getPublicKey());
	}

	@Test
	@DisplayName("user is mapped to save response")
	void toLoginSaveResDtoMapsUser() {
		// given
		User user = createUser();

		// when
		CliLoginSaveResDto resDto = CliAuthConverter.toLoginSaveResDto(user);

		// then
		assertEquals("123456", resDto.githubId());
		assertEquals("user@example.com", resDto.email());
	}

	private CliLoginSaveReqDto saveRequest(final String deviceName) {
		return CliLoginSaveReqDto.builder()
			.loginSessionId("session-id")
			.githubId("123456")
			.deviceName(deviceName)
			.publicKey("ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAITestKey")
			.build();
	}

	private User createUser() {
		return User.builder()
			.id(1L)
			.githubId("123456")
			.email("user@example.com")
			.role(UserRole.VIEWER)
			.build();
	}
}
