package io.envio.auth.domain.cli.service.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.envio.auth.domain.cli.dto.request.CliLoginSaveReqDto;
import io.envio.auth.domain.cli.dto.request.CliLoginStatusReqDto;
import io.envio.auth.domain.cli.dto.response.CliLoginSaveResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStartResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStatusResDto;
import io.envio.auth.domain.cli.entity.RedisCliSession;
import io.envio.auth.domain.cli.service.command.CliAuthCommandService;
import io.envio.auth.domain.cli.service.query.CliAuthQueryService;

@ExtendWith(MockitoExtension.class)
@DisplayName("CLI Auth Facade Service")
class CliAuthFacadeServiceImplTest {

	private static final String SESSION_ID = "session-id";

	@Mock
	private CliAuthCommandService commandService;

	@Mock
	private CliAuthQueryService queryService;

	private CliAuthFacadeServiceImpl facadeService;

	@BeforeEach
	void setUp() {
		facadeService = new CliAuthFacadeServiceImpl(commandService, queryService);
	}

	@Test
	@DisplayName("start login delegates to command service")
	void startLoginReturnsCommandServiceResponse() {
		// given
		CliLoginStartResDto startResponse = CliLoginStartResDto.builder()
			.loginSessionId(SESSION_ID)
			.loginUrl("https://github.com/login")
			.build();
		when(commandService.createLoginSession()).thenReturn(startResponse);

		// when
		CliLoginStartResDto result = facadeService.startLogin();

		// then
		assertEquals(startResponse, result);
	}

	@Test
	@DisplayName("login status maps Redis session to status response")
	void getLoginStatusReturnsStatusResponse() {
		// given
		CliLoginStatusReqDto reqDto = new CliLoginStatusReqDto(SESSION_ID);
		when(queryService.getSession(SESSION_ID)).thenReturn(successSession());

		// when
		CliLoginStatusResDto result = facadeService.getLoginStatus(reqDto);

		// then
		assertEquals(RedisCliSession.STATUS_SUCCESS, result.status());
		assertEquals("123456", result.githubId());
		assertEquals("user@example.com", result.email());
	}

	@Test
	@DisplayName("successful save deletes login session")
	void saveCliUserDeletesSessionWhenRegisterSucceeds() {
		// given
		CliLoginSaveReqDto reqDto = saveRequest();
		RedisCliSession session = successSession();
		CliLoginSaveResDto saveResponse = CliLoginSaveResDto.builder()
			.githubId("123456")
			.email("user@example.com")
			.build();

		when(queryService.reserveSessionForSave(SESSION_ID, "123456")).thenReturn(session);
		when(commandService.registerUserAndDevice(reqDto, session)).thenReturn(saveResponse);

		// when
		CliLoginSaveResDto result = facadeService.saveCliUser(reqDto);

		// then
		assertEquals(saveResponse, result);
		verify(queryService).deleteSession(SESSION_ID);
		verify(queryService, never()).releaseSessionSaveReservation(SESSION_ID);
	}

	@Test
	@DisplayName("failed save releases save reservation")
	void saveCliUserReleasesReservationWhenRegisterFails() {
		// given
		CliLoginSaveReqDto reqDto = saveRequest();
		RedisCliSession session = successSession();
		RuntimeException failure = new RuntimeException("save failed");

		when(queryService.reserveSessionForSave(SESSION_ID, "123456")).thenReturn(session);
		when(commandService.registerUserAndDevice(reqDto, session)).thenThrow(failure);

		// when
		RuntimeException exception = assertThrows(
			RuntimeException.class,
			() -> facadeService.saveCliUser(reqDto)
		);

		// then
		assertEquals(failure, exception);
		verify(queryService).releaseSessionSaveReservation(SESSION_ID);
		verify(queryService, never()).deleteSession(SESSION_ID);
	}

	private CliLoginSaveReqDto saveRequest() {
		return CliLoginSaveReqDto.builder()
			.loginSessionId(SESSION_ID)
			.githubId("123456")
			.deviceName("Laptop")
			.publicKey("ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAITestKey")
			.build();
	}

	private RedisCliSession successSession() {
		return RedisCliSession.builder()
			.id(SESSION_ID)
			.status(RedisCliSession.STATUS_SUCCESS)
			.githubId("123456")
			.email("user@example.com")
			.expiresIn(300)
			.build();
	}
}
