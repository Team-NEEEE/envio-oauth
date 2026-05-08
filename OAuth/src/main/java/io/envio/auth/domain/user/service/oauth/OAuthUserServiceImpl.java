package io.envio.auth.domain.user.service.oauth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.exception.UserException;
import io.envio.auth.domain.user.service.command.UserCommandService;
import io.envio.auth.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthUserServiceImpl implements OAuthUserService {

	private final UserCommandService commandService;
	private final UserQueryService queryService;

	@Override
	public User findOrCreateGithubUser(final String githubId, final String email) {
		return queryService.findOptionalByGithubId(githubId)
			.orElseGet(() -> saveOrFindGithubUser(githubId, email));
	}

	private User saveOrFindGithubUser(final String githubId, final String email) {
		try {
			return commandService.save(User.createGithubUser(githubId, email));
		} catch (DataIntegrityViolationException exception) {
			return queryService.findOptionalByGithubId(githubId)
				.orElseThrow(() -> new UserException(ErrorCode.USER_ALREADY_EXISTS));
		}
	}
}
