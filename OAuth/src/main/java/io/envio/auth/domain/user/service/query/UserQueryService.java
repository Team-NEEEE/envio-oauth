package io.envio.auth.domain.user.service.query;

import io.envio.auth.domain.user.entity.User;

public interface UserQueryService {

	User findById(final Long userId);

	User findByGithubId(final String githubId);
}
