package io.envio.auth.domain.user.service.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserRole;
import io.envio.auth.domain.user.service.command.UserCommandService;
import io.envio.auth.domain.user.service.query.UserQueryService;

@DisplayName("OAuth 사용자 서비스")
class OAuthUserServiceImplTest {

	private final UserCommandService commandService = org.mockito.Mockito.mock(UserCommandService.class);
	private final UserQueryService queryService = org.mockito.Mockito.mock(UserQueryService.class);
	private final OAuthUserServiceImpl oauthUserService = new OAuthUserServiceImpl(commandService, queryService);

	@Test
	@DisplayName("GitHub ID로 기존 사용자를 조회한다")
	void findOrCreateGithubUserReturnsExistingUser() {
		User existingUser = createUser("123456", "user@example.com");
		when(queryService.findOptionalByGithubId("123456")).thenReturn(Optional.of(existingUser));

		User result = oauthUserService.findOrCreateGithubUser("123456", "user@example.com");

		assertEquals(existingUser, result);
		verify(commandService, never()).createGithubUser("123456", "user@example.com");
	}

	@Test
	@DisplayName("GitHub ID로 사용자가 없으면 새 사용자를 생성한다")
	void findOrCreateGithubUserCreatesUserWhenMissing() {
		User createdUser = createUser("123456", "user@example.com");
		when(queryService.findOptionalByGithubId("123456")).thenReturn(Optional.empty());
		when(commandService.createGithubUser("123456", "user@example.com")).thenReturn(createdUser);

		User result = oauthUserService.findOrCreateGithubUser("123456", "user@example.com");

		assertEquals(createdUser, result);
		verify(commandService).createGithubUser("123456", "user@example.com");
	}

	@Test
	@DisplayName("동시 생성 충돌이 발생하면 다시 조회한 사용자를 반환한다")
	void findOrCreateGithubUserReturnsUserAfterDuplicateInsertConflict() {
		User existingUser = createUser("123456", "user@example.com");
		when(queryService.findOptionalByGithubId("123456"))
			.thenReturn(Optional.empty())
			.thenReturn(Optional.of(existingUser));
		when(commandService.createGithubUser("123456", "user@example.com"))
			.thenThrow(new DataIntegrityViolationException("duplicate github id"));

		User result = oauthUserService.findOrCreateGithubUser("123456", "user@example.com");

		assertEquals(existingUser, result);
		verify(commandService).createGithubUser("123456", "user@example.com");
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
