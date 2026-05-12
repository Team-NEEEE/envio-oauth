package io.envio.auth.domain.cli.service.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.common.error.exception.BusinessException;
import io.envio.auth.domain.cli.entity.RedisCliSession;
import io.envio.auth.domain.cli.repository.RedisCliSessionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CLI Auth Query Service")
class CliAuthQueryServiceImplTest {

	private static final String SESSION_ID = "session-id";
	private static final String LOCK_KEY = "cli_session_save_lock:" + SESSION_ID;

	@Mock
	private RedisCliSessionRepository redisCliSessionRepository;

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	private CliAuthQueryServiceImpl queryService;

	@BeforeEach
	void setUp() {
		queryService = new CliAuthQueryServiceImpl(redisCliSessionRepository, stringRedisTemplate);
	}

	@Test
	@DisplayName("session is returned by login session id")
	void getSessionReturnsSession() {
		// given
		RedisCliSession session = successSession("123456");
		when(redisCliSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

		// when
		RedisCliSession result = queryService.getSession(SESSION_ID);

		// then
		assertEquals(session, result);
	}

	@Test
	@DisplayName("missing session throws invalid session exception")
	void getSessionThrowsExceptionWhenSessionIsMissing() {
		// given
		when(redisCliSessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> queryService.getSession(SESSION_ID)
		);

		// then
		assertEquals(ErrorCode.CLI_LOGIN_SESSION_INVALID, exception.getErrorCode());
	}

	@Test
	@DisplayName("save reservation succeeds when lock and session validation pass")
	void reserveSessionForSaveReturnsSessionWhenValidationPasses() {
		// given
		RedisCliSession session = successSession("123456");
		mockSaveLock(true);
		when(redisCliSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

		// when
		RedisCliSession result = queryService.reserveSessionForSave(SESSION_ID, "123456");

		// then
		assertEquals(session, result);
		verify(stringRedisTemplate, never()).delete(anyString());
	}

	@Test
	@DisplayName("already acquired save lock throws already processing exception")
	void reserveSessionForSaveThrowsExceptionWhenLockAlreadyExists() {
		// given
		mockSaveLock(false);

		// when
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> queryService.reserveSessionForSave(SESSION_ID, "123456")
		);

		// then
		assertEquals(ErrorCode.CLI_LOGIN_SESSION_ALREADY_PROCESSING, exception.getErrorCode());
		verify(redisCliSessionRepository, never()).findById(SESSION_ID);
		verify(stringRedisTemplate, never()).delete(anyString());
	}

	@Test
	@DisplayName("not ready session releases save lock and throws exception")
	void reserveSessionForSaveReleasesLockWhenSessionIsNotReady() {
		// given
		mockSaveLock(true);
		when(redisCliSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(pendingSession()));

		// when
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> queryService.reserveSessionForSave(SESSION_ID, "123456")
		);

		// then
		assertEquals(ErrorCode.CLI_LOGIN_SESSION_NOT_READY, exception.getErrorCode());
		verify(stringRedisTemplate).delete(LOCK_KEY);
	}

	@Test
	@DisplayName("github id mismatch releases save lock and throws exception")
	void reserveSessionForSaveReleasesLockWhenGithubIdMismatches() {
		// given
		mockSaveLock(true);
		when(redisCliSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(successSession("123456")));

		// when
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> queryService.reserveSessionForSave(SESSION_ID, "654321")
		);

		// then
		assertEquals(ErrorCode.CLI_LOGIN_SESSION_MISMATCH, exception.getErrorCode());
		verify(stringRedisTemplate).delete(LOCK_KEY);
	}

	@Test
	@DisplayName("delete session removes Redis session and save lock")
	void deleteSessionDeletesSessionAndSaveLock() {
		// when
		queryService.deleteSession(SESSION_ID);

		// then
		verify(redisCliSessionRepository).deleteById(SESSION_ID);
		verify(stringRedisTemplate).delete(LOCK_KEY);
	}

	@Test
	@DisplayName("release save reservation removes save lock")
	void releaseSessionSaveReservationDeletesSaveLock() {
		// when
		queryService.releaseSessionSaveReservation(SESSION_ID);

		// then
		verify(stringRedisTemplate).delete(LOCK_KEY);
	}

	private void mockSaveLock(final boolean acquired) {
		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(LOCK_KEY, "1", Duration.ofSeconds(300))).thenReturn(acquired);
	}

	private RedisCliSession pendingSession() {
		return RedisCliSession.builder()
			.id(SESSION_ID)
			.status(RedisCliSession.STATUS_PENDING)
			.expiresIn(300)
			.build();
	}

	private RedisCliSession successSession(final String githubId) {
		return RedisCliSession.builder()
			.id(SESSION_ID)
			.status(RedisCliSession.STATUS_SUCCESS)
			.githubId(githubId)
			.email("user@example.com")
			.expiresIn(300)
			.build();
	}
}
