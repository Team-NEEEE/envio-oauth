package io.envio.auth.domain.user.service.oauth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.auth.domain.user.entity.User;
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
			.orElseGet(() -> userRepository.save(User.createGithubUser(githubId, email)));
	}
}
