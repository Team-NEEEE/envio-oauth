package io.envio.auth.domain.view.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.auth.common.config.properties.JwtProperties;
import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.common.error.exception.BusinessException;
import io.envio.auth.common.security.jwt.JwtClaims;
import io.envio.auth.common.security.jwt.JwtParsingException;
import io.envio.auth.common.security.jwt.JwtTokenProvider;
import io.envio.auth.common.security.token.TokenRepository;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserDevice;
import io.envio.auth.domain.user.service.query.UserDeviceQueryService;
import io.envio.auth.domain.user.service.query.UserQueryService;
import io.envio.auth.domain.view.dto.request.AuthRefreshReqDto;
import io.envio.auth.domain.view.dto.response.AuthMeResDto;
import io.envio.auth.domain.view.dto.response.AuthRefreshResDto;
import io.envio.auth.domain.view.dto.response.OAuthLoginResDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ViewAuthServiceImpl implements ViewAuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtProperties jwtProperties;
	private final TokenRepository tokenRepository;
	private final UserQueryService userQueryService;
	private final UserDeviceQueryService userDeviceQueryService;

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

	@Override
	@Transactional(readOnly = true)
	public AuthMeResDto getCurrentUser(final JwtClaims claims) {
		User user = userQueryService.findById(claims.userId());
		String publicKey = userDeviceQueryService.findLatestByUserId(user.getId())
			.map(UserDevice::getPublicKey)
			.orElse(null);

		return AuthMeResDto.builder()
			.userId(user.getId())
			.githubId(user.getGithubId())
			.email(user.getEmail())
			.role(user.getRole().name())
			.publicKey(publicKey)
			.build();
	}

	@Override
	public AuthRefreshResDto refreshToken(final AuthRefreshReqDto reqDto) {
		JwtClaims claims = parseRefreshToken(reqDto.refreshToken());
		String tokenKey = String.valueOf(claims.userId());
		String savedRefreshToken = tokenRepository.find(tokenKey)
			.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

		if (!matchesToken(savedRefreshToken, reqDto.refreshToken())) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		User user = userQueryService.findById(claims.userId());
		String accessToken = jwtTokenProvider.createAccessToken(
			user.getId(),
			user.getGithubId(),
			user.getEmail(),
			user.getRole().name()
		);
		String refreshToken = jwtTokenProvider.createRefreshToken(
			user.getId(),
			user.getGithubId(),
			user.getEmail(),
			user.getRole().name()
		);
		tokenRepository.save(tokenKey, refreshToken, jwtProperties.refreshTokenExpiration());

		return AuthRefreshResDto.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	private JwtClaims parseRefreshToken(final String refreshToken) {
		try {
			return jwtTokenProvider.parseRefreshToken(refreshToken);
		} catch (JwtParsingException exception) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
	}

	private boolean matchesToken(final String savedRefreshToken, final String requestRefreshToken) {
		return MessageDigest.isEqual(
			savedRefreshToken.getBytes(StandardCharsets.UTF_8),
			requestRefreshToken.getBytes(StandardCharsets.UTF_8)
		);
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
