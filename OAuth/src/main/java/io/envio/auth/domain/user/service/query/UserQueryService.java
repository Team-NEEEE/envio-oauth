package io.envio.auth.domain.user.service.query;

import java.util.Optional;

import io.envio.auth.domain.user.entity.User;

public interface UserQueryService {

	User findById(final Long userId);

	User findByGithubId(final String githubId);

	Optional<User> findOptionalByGithubId(final String githubId);

	boolean existsByGithubId(final String githubId);
}
