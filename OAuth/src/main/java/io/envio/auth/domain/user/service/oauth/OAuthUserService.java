package io.envio.auth.domain.user.service.oauth;

import io.envio.auth.domain.user.entity.User;

public interface OAuthUserService {

	User findOrCreateGithubUser(final String githubId, final String email);
}
