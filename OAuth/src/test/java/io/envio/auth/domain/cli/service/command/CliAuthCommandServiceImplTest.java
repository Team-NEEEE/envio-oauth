package io.envio.auth.domain.cli.service.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.common.error.exception.BusinessException;
import io.envio.auth.domain.cli.dto.request.CliLoginSaveReqDto;
import io.envio.auth.domain.cli.dto.response.CliLoginSaveResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStartResDto;
import io.envio.auth.domain.cli.entity.RedisCliSession;
import io.envio.auth.domain.cli.repository.RedisCliSessionRepository;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserDevice;
import io.envio.auth.domain.user.entity.UserRole;
import io.envio.auth.domain.user.repository.UserDeviceRepository;
import io.envio.auth.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CLI Auth Command Service")
class CliAuthCommandServiceImplTest {

	private static final String GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
	private static final String GITHUB_USER_URL = "https://api.github.com/user";

	@Mock
	private RedisCliSessionRepository redisCliSessionRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserDeviceRepository userDeviceRepository;

	@Mock
	private RestTemplate restTemplate;

	private CliAuthCommandServiceImpl commandService;

	@BeforeEach
	void setUp() {
		commandService = new CliAuthCommandServiceImpl(
			redisCliSessionRepository,
			userRepository,
			userDeviceRepository,
			restTemplate
		);
		ReflectionTestUtils.setField(commandService, "clientId", "client-id");
		ReflectionTestUtils.setField(commandService, "clientSecret", "client-secret");
		ReflectionTestUtils.setField(commandService, "redirectUri", "http://localhost:8080/callback");
	}

	@Test
	@DisplayName("login session is created as PENDING and GitHub OAuth url is returned")
	void createLoginSessionSavesPendingSessionAndReturnsAuthorizeUrl() {
		// given
		when(redisCliSessionRepository.save(any(RedisCliSession.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		// when
		CliLoginStartResDto result = commandService.createLoginSession();

		// then
		ArgumentCaptor<RedisCliSession> sessionCaptor = ArgumentCaptor.forClass(RedisCliSession.class);
		verify(redisCliSessionRepository).save(sessionCaptor.capture());

		RedisCliSession savedSession = sessionCaptor.getValue();
		assertNotNull(savedSession.getId());
		assertFalse(savedSession.getId().isBlank());
		assertEquals(RedisCliSession.STATUS_PENDING, savedSession.getStatus());
		assertEquals(300, savedSession.getExpiresIn());
		assertEquals(savedSession.getId(), result.loginSessionId());
		assertTrue(result.loginUrl().startsWith("https://github.com/login/oauth/authorize"));
		assertTrue(result.loginUrl().contains("client_id=client-id"));
		assertTrue(result.loginUrl().contains("state=" + savedSession.getId()));
	}

	@Test
	@DisplayName("successful GitHub callback completes session with GitHub user info")
	void processGithubCallbackCompletesSession() {
		// given
		RedisCliSession session = pendingSession("session-id");
		Map<String, Object> tokenResponse = Map.of("access_token", "access-token");
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("id", 123456);
		userInfo.put("login", "octocat");
		userInfo.put("email", "user@example.com");

		when(redisCliSessionRepository.findById("session-id")).thenReturn(Optional.of(session));
		when(restTemplate.postForObject(eq(GITHUB_ACCESS_TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
			.thenReturn(tokenResponse);
		when(restTemplate.exchange(eq(GITHUB_USER_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
			.thenReturn(ResponseEntity.ok(userInfo));

		// when
		commandService.processGithubCallback("code", "session-id");

		// then
		assertEquals(RedisCliSession.STATUS_SUCCESS, session.getStatus());
		assertEquals("123456", session.getGithubId());
		assertEquals("user@example.com", session.getEmail());
		verify(redisCliSessionRepository).save(session);
	}

	@Test
	@DisplayName("blank GitHub email is replaced with noreply email")
	void processGithubCallbackUsesNoreplyEmailWhenGithubEmailIsBlank() {
		// given
		RedisCliSession session = pendingSession("session-id");
		Map<String, Object> tokenResponse = Map.of("access_token", "access-token");
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("id", 123456);
		userInfo.put("login", "octocat");
		userInfo.put("email", "");

		when(redisCliSessionRepository.findById("session-id")).thenReturn(Optional.of(session));
		when(restTemplate.postForObject(eq(GITHUB_ACCESS_TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
			.thenReturn(tokenResponse);
		when(restTemplate.exchange(eq(GITHUB_USER_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
			.thenReturn(ResponseEntity.ok(userInfo));

		// when
		commandService.processGithubCallback("code", "session-id");

		// then
		assertEquals("octocat@users.noreply.github.com", session.getEmail());
		verify(redisCliSessionRepository).save(session);
	}

	@Test
	@DisplayName("null GitHub email is replaced with noreply email")
	void processGithubCallbackUsesNoreplyEmailWhenGithubEmailIsNull() {
		// given
		RedisCliSession session = pendingSession("session-id");
		Map<String, Object> tokenResponse = Map.of("access_token", "access-token");
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("id", 123456);
		userInfo.put("login", "octocat");
		userInfo.put("email", null);

		when(redisCliSessionRepository.findById("session-id")).thenReturn(Optional.of(session));
		when(restTemplate.postForObject(eq(GITHUB_ACCESS_TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
			.thenReturn(tokenResponse);
		when(restTemplate.exchange(eq(GITHUB_USER_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
			.thenReturn(ResponseEntity.ok(userInfo));

		// when
		commandService.processGithubCallback("code", "session-id");

		// then
		assertEquals("octocat@users.noreply.github.com", session.getEmail());
		verify(redisCliSessionRepository).save(session);
	}

	@Test
	@DisplayName("missing callback session throws invalid session exception")
	void processGithubCallbackThrowsExceptionWhenSessionIsMissing() {
		// given
		when(redisCliSessionRepository.findById("missing-session")).thenReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> commandService.processGithubCallback("code", "missing-session")
		);

		// then
		assertEquals(ErrorCode.CLI_LOGIN_SESSION_INVALID, exception.getErrorCode());
		verifyNoInteractions(restTemplate);
	}

	@Test
	@DisplayName("non pending callback session throws not ready exception")
	void processGithubCallbackThrowsExceptionWhenSessionIsNotPending() {
		// given
		when(redisCliSessionRepository.findById("session-id"))
			.thenReturn(Optional.of(successSession("session-id", "123456", "user@example.com")));

		// when
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> commandService.processGithubCallback("code", "session-id")
		);

		// then
		assertEquals(ErrorCode.CLI_LOGIN_SESSION_NOT_READY, exception.getErrorCode());
		verifyNoInteractions(restTemplate);
	}

	@Test
	@DisplayName("missing access token throws GitHub OAuth exception")
	void processGithubCallbackThrowsExceptionWhenAccessTokenIsMissing() {
		// given
		when(redisCliSessionRepository.findById("session-id")).thenReturn(Optional.of(pendingSession("session-id")));
		when(restTemplate.postForObject(eq(GITHUB_ACCESS_TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
			.thenReturn(Map.of());

		// when
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> commandService.processGithubCallback("code", "session-id")
		);

		// then
		assertEquals(ErrorCode.GITHUB_OAUTH_FAILED, exception.getErrorCode());
	}

	@Test
	@DisplayName("new user and CLI device are registered from authenticated session")
	void registerUserAndDeviceCreatesUserAndDevice() {
		// given
		RedisCliSession session = successSession("session-id", "123456", "user@example.com");
		CliLoginSaveReqDto reqDto = saveRequest("123456", "Laptop");
		User savedUser = createUser(1L, "123456", "user@example.com");

		when(userRepository.findByGithubId("123456")).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(userDeviceRepository.existsByUserAndDeviceName(savedUser, "Laptop")).thenReturn(false);

		// when
		CliLoginSaveResDto result = commandService.registerUserAndDevice(reqDto, session);

		// then
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		ArgumentCaptor<UserDevice> deviceCaptor = ArgumentCaptor.forClass(UserDevice.class);
		verify(userRepository).save(userCaptor.capture());
		verify(userDeviceRepository).save(deviceCaptor.capture());

		assertEquals("123456", userCaptor.getValue().getGithubId());
		assertEquals("user@example.com", userCaptor.getValue().getEmail());
		assertEquals(UserRole.VIEWER, userCaptor.getValue().getRole());
		assertEquals(savedUser, deviceCaptor.getValue().getUser());
		assertEquals("Laptop", deviceCaptor.getValue().getDeviceName());
		assertEquals("ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAITestKey", deviceCaptor.getValue().getPublicKey());
		assertEquals("123456", result.githubId());
		assertEquals("user@example.com", result.email());
	}

	@Test
	@DisplayName("existing user email is updated before CLI device registration")
	void registerUserAndDeviceUpdatesExistingUserEmail() {
		// given
		RedisCliSession session = successSession("session-id", "123456", "new@example.com");
		CliLoginSaveReqDto reqDto = saveRequest("123456", "Desktop");
		User existingUser = createUser(1L, "123456", "old@example.com");

		when(userRepository.findByGithubId("123456")).thenReturn(Optional.of(existingUser));
		when(userDeviceRepository.existsByUserAndDeviceName(existingUser, "Desktop")).thenReturn(false);

		// when
		CliLoginSaveResDto result = commandService.registerUserAndDevice(reqDto, session);

		// then
		assertEquals("new@example.com", existingUser.getEmail());
		assertEquals("new@example.com", result.email());
		verify(userRepository, never()).save(any(User.class));
		verify(userDeviceRepository).save(any(UserDevice.class));
	}

	@Test
	@DisplayName("duplicated device name throws already exists exception")
	void registerUserAndDeviceThrowsExceptionWhenDeviceNameAlreadyExists() {
		// given
		RedisCliSession session = successSession("session-id", "123456", "user@example.com");
		CliLoginSaveReqDto reqDto = saveRequest("123456", "Duplicated");
		User existingUser = createUser(1L, "123456", "user@example.com");

		when(userRepository.findByGithubId("123456")).thenReturn(Optional.of(existingUser));
		when(userDeviceRepository.existsByUserAndDeviceName(existingUser, "Duplicated")).thenReturn(true);

		// when
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> commandService.registerUserAndDevice(reqDto, session)
		);

		// then
		assertEquals(ErrorCode.CLI_DEVICE_ALREADY_EXISTS, exception.getErrorCode());
		verify(userDeviceRepository, never()).save(any(UserDevice.class));
	}

	@Test
	@DisplayName("duplicate user insert conflict returns existing user and registers device")
	void registerUserAndDeviceFindsUserAfterDuplicateInsertConflict() {
		// given
		RedisCliSession session = successSession("session-id", "123456", "user@example.com");
		CliLoginSaveReqDto reqDto = saveRequest("123456", "Laptop");
		User existingUser = createUser(1L, "123456", "user@example.com");

		when(userRepository.findByGithubId("123456"))
			.thenReturn(Optional.empty())
			.thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class)))
			.thenThrow(new DataIntegrityViolationException("duplicate github id"));
		when(userDeviceRepository.existsByUserAndDeviceName(existingUser, "Laptop")).thenReturn(false);

		// when
		CliLoginSaveResDto result = commandService.registerUserAndDevice(reqDto, session);

		// then
		assertEquals("123456", result.githubId());
		verify(userDeviceRepository).save(any(UserDevice.class));
	}

	private RedisCliSession pendingSession(final String sessionId) {
		return RedisCliSession.builder()
			.id(sessionId)
			.status(RedisCliSession.STATUS_PENDING)
			.expiresIn(300)
			.build();
	}

	private RedisCliSession successSession(final String sessionId, final String githubId, final String email) {
		return RedisCliSession.builder()
			.id(sessionId)
			.status(RedisCliSession.STATUS_SUCCESS)
			.githubId(githubId)
			.email(email)
			.expiresIn(300)
			.build();
	}

	private CliLoginSaveReqDto saveRequest(final String githubId, final String deviceName) {
		return CliLoginSaveReqDto.builder()
			.loginSessionId("session-id")
			.githubId(githubId)
			.deviceName(deviceName)
			.publicKey("ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAITestKey")
			.build();
	}

	private User createUser(final Long id, final String githubId, final String email) {
		return User.builder()
			.id(id)
			.githubId(githubId)
			.email(email)
			.role(UserRole.VIEWER)
			.build();
	}
}
