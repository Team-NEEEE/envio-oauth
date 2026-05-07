package io.envio.auth.common.security.oauth;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.service.oauth.OAuthUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GitHubOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private static final String GITHUB_LOGIN_ATTRIBUTE = "login";
	private static final String GITHUB_EMAIL_ATTRIBUTE = "email";
	private static final String ROLE_PREFIX = "ROLE_";

	private final OAuthUserService oauthUserService;
	private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

	@Override
	public OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oauth2User = delegate.loadUser(userRequest);
		Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());

		String githubId = String.valueOf(attributes.get(GITHUB_LOGIN_ATTRIBUTE));
		String email = resolveEmail(attributes);
		User user = oauthUserService.findOrCreateGithubUser(githubId, email);

		attributes.put("userId", user.getId());
		attributes.put(GITHUB_EMAIL_ATTRIBUTE, user.getEmail());
		attributes.put("role", user.getRole().name());

		return new DefaultOAuth2User(
			Set.of(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name())),
			attributes,
			GITHUB_LOGIN_ATTRIBUTE
		);
	}

	private String resolveEmail(final Map<String, Object> attributes) {
		Object email = attributes.get(GITHUB_EMAIL_ATTRIBUTE);
		if (email instanceof String emailValue && !emailValue.isBlank()) {
			return emailValue;
		}

		return attributes.get(GITHUB_LOGIN_ATTRIBUTE) + "@users.noreply.github.com";
	}
}
