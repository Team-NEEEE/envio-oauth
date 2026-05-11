package io.envio.auth.domain.view.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import io.envio.auth.common.config.properties.JwtProperties;
import io.envio.auth.common.security.jwt.JwtTokenProvider;
import io.envio.auth.common.security.token.TokenRepository;
import io.envio.auth.domain.view.dto.response.OAuthLoginResDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ViewAuthServiceImpl implements ViewAuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtProperties jwtProperties;
	private final TokenRepository tokenRepository;

	@Override
	public OAuthLoginResDto issueOAuthLoginTokens(final Authentication authentication) {
		OAuth2User oauth2User = (OAuth2User)authentication.getPrincipal();
		Long userId = getRequiredLongAttribute(oauth2User, "userId");
		String githubId = getRequiredStringAttribute(oauth2User, "githubId");
		String email = getRequiredStringAttribute(oauth2User, "email");
		String role = getRequiredStringAttribute(oauth2User, "role");

		String accessToken = jwtTokenProvider.createAccessToken(userId, githubId, email, role);
		String refreshToken = jwtTokenProvider.createRefreshToken(userId, githubId, email, role);
		Duration refreshTokenExpiration = jwtProperties.refreshTokenExpiration();
		tokenRepository.save(String.valueOf(userId), refreshToken, refreshTokenExpiration);

		return OAuthLoginResDto.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.userId(userId)
			.email(email)
			.role(role)
			.build();
	}

	private Long getRequiredLongAttribute(final OAuth2User oauth2User, final String attributeName) {
		return Optional.ofNullable((Number)oauth2User.getAttribute(attributeName))
			.map(Number::longValue)
			.orElseThrow(() -> new IllegalStateException(attributeName + " attribute is missing from OAuth2User"));
	}

	private String getRequiredStringAttribute(final OAuth2User oauth2User, final String attributeName) {
		return Optional.ofNullable((String)oauth2User.getAttribute(attributeName))
			.orElseThrow(() -> new IllegalStateException(attributeName + " attribute is missing from OAuth2User"));
	}
}
