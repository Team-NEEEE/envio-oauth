package io.envio.auth.domain.view.service;

import java.time.Duration;

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
		Long userId = ((Number)oauth2User.getAttribute("userId")).longValue();
		String githubId = oauth2User.getAttribute("githubId");
		String email = oauth2User.getAttribute("email");
		String role = oauth2User.getAttribute("role");

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
}
