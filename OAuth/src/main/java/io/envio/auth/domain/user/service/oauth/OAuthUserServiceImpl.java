package io.envio.auth.domain.user.service.oauth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.exception.UserException;
import io.envio.auth.domain.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthUserServiceImpl implements OAuthUserService {

	private final UserRepository userRepository;

	@Override
	public User findOrCreateGithubUser(final String githubId, final String email) {
		return userRepository.findByGithubId(githubId)
			.orElseGet(() -> saveOrFindGithubUser(githubId, email));
	}

	private User saveOrFindGithubUser(final String githubId, final String email) {
		try {
			return userRepository.save(User.createGithubUser(githubId, email));
		} catch (DataIntegrityViolationException exception) {
			return userRepository.findByGithubId(githubId)
				.orElseThrow(() -> new UserException(ErrorCode.USER_ALREADY_EXISTS));
		}
	}
}
