package io.envio.auth.domain.user.service.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserRole;
import io.envio.auth.domain.user.service.command.UserCommandService;
import io.envio.auth.domain.user.service.query.UserQueryService;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth 사용자 서비스")
class OAuthUserServiceImplTest {

	@Mock
	private UserCommandService commandService;

	@Mock
	private UserQueryService queryService;

	@InjectMocks
	private OAuthUserServiceImpl oauthUserService;

	@Test
	@DisplayName("GitHub ID로 기존 사용자를 조회한다")
	void findOrCreateGithubUserReturnsExistingUser() {
		// given
		User existingUser = createUser("123456", "user@example.com");
		when(queryService.findOptionalByGithubId("123456")).thenReturn(Optional.of(existingUser));

		// when
		User result = oauthUserService.findOrCreateGithubUser("123456", "user@example.com");

		// then
		assertEquals(existingUser, result);
		verify(commandService, never()).save(any(User.class));
	}

	@Test
	@DisplayName("GitHub ID로 사용자가 없으면 새 사용자를 생성한다")
	void findOrCreateGithubUserCreatesUserWhenMissing() {
		// given
		User createdUser = createUser("123456", "user@example.com");
		when(queryService.findOptionalByGithubId("123456")).thenReturn(Optional.empty());
		when(commandService.save(any(User.class))).thenReturn(createdUser);

		// when
		User result = oauthUserService.findOrCreateGithubUser("123456", "user@example.com");

		// then
		assertEquals(createdUser, result);
		verify(commandService).save(any(User.class));
	}

	@Test
	@DisplayName("동시 생성 충돌이 발생하면 다시 조회한 사용자를 반환한다")
	void findOrCreateGithubUserReturnsUserAfterDuplicateInsertConflict() {
		// given
		User existingUser = createUser("123456", "user@example.com");
		when(queryService.findOptionalByGithubId("123456"))
			.thenReturn(Optional.empty())
			.thenReturn(Optional.of(existingUser));
		when(commandService.save(any(User.class)))
			.thenThrow(new DataIntegrityViolationException("duplicate github id"));

		// when
		User result = oauthUserService.findOrCreateGithubUser("123456", "user@example.com");

		// then
		assertEquals(existingUser, result);
		verify(commandService).save(any(User.class));
	}

	private User createUser(final String githubId, final String email) {
		return User.builder()
			.id(1L)
			.githubId(githubId)
			.email(email)
			.role(UserRole.VIEWER)
			.build();
	}
}
