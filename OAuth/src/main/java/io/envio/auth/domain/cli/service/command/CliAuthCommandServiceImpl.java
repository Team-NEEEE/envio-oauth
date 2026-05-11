package io.envio.auth.domain.cli.service.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.common.error.exception.BusinessException;
import io.envio.auth.domain.cli.converter.CliAuthConverter;
import io.envio.auth.domain.cli.dto.request.CliLoginSaveReqDto;
import io.envio.auth.domain.cli.dto.response.CliLoginSaveResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStartResDto;
import io.envio.auth.domain.cli.entity.RedisCliSession;
import io.envio.auth.domain.cli.repository.RedisCliSessionRepository;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserDevice;
import io.envio.auth.domain.user.repository.UserDeviceRepository;
import io.envio.auth.domain.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliAuthCommandServiceImpl implements CliAuthCommandService {

	private static final int EXPIRES_IN = 300;
	private static final String GITHUB_ID_ATTRIBUTE = "id";
	private static final String GITHUB_LOGIN_ATTRIBUTE = "login";
	private static final String GITHUB_EMAIL_ATTRIBUTE = "email";
	private static final String GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
	private static final String GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
	private static final String GITHUB_USER_URL = "https://api.github.com/user";

	private final RedisCliSessionRepository redisCliSessionRepository;
	private final UserRepository userRepository;
	private final UserDeviceRepository userDeviceRepository;
	@Qualifier("cliRestTemplate")
	private final RestTemplate restTemplate;

	@Value("${spring.security.oauth2.client.registration.github.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.github.client-secret}")
	private String clientSecret;

	@Value("${auth.oauth2.cli-redirect-uri}")
	private String redirectUri;

	@Override
	public CliLoginStartResDto createLoginSession() {
		String sessionId = UUID.randomUUID().toString();
		String authUrl = UriComponentsBuilder.fromUriString(GITHUB_AUTHORIZE_URL)
			.queryParam("client_id", clientId)
			.queryParam("redirect_uri", redirectUri)
			.queryParam("state", sessionId)
			.queryParam("scope", "read:user user:email")
			.encode()
			.toUriString();

		RedisCliSession session = RedisCliSession.builder()
			.id(sessionId)
			.status(RedisCliSession.STATUS_PENDING)
			.expiresIn(EXPIRES_IN)
			.build();

		redisCliSessionRepository.save(session);
		log.info("[CliAuth] login session created - sessionId: {}", sessionId);

		return CliAuthConverter.toLoginStartResDto(sessionId, authUrl);
	}

	@Override
	public void processGithubCallback(final String code, final String loginSessionId) {
		log.info("[CliAuth] GitHub callback received - sessionId: {}", loginSessionId);
		RedisCliSession session = redisCliSessionRepository.findById(loginSessionId)
			.orElseThrow(() -> new BusinessException(ErrorCode.CLI_LOGIN_SESSION_INVALID));

		if (!RedisCliSession.STATUS_PENDING.equals(session.getStatus())) {
			throw new BusinessException(ErrorCode.CLI_LOGIN_SESSION_NOT_READY);
		}

		Map<String, String> tokenParams = new HashMap<>();
		tokenParams.put("client_id", clientId);
		tokenParams.put("client_secret", clientSecret);
		tokenParams.put("code", code);
		tokenParams.put("redirect_uri", redirectUri);

		HttpHeaders tokenHeaders = new HttpHeaders();
		tokenHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);

		Map<String, Object> tokenResponse;
		try {
			tokenResponse = restTemplate.postForObject(
				GITHUB_ACCESS_TOKEN_URL,
				tokenRequest,
				Map.class
			);
		} catch (RestClientException exception) {
			log.warn("[CliAuth] GitHub access token request failed - sessionId: {}", loginSessionId, exception);
			throw new BusinessException(ErrorCode.GITHUB_OAUTH_FAILED);
		}

		if (tokenResponse == null || tokenResponse.get("access_token") == null) {
			throw new BusinessException(ErrorCode.GITHUB_OAUTH_FAILED);
		}
		String accessToken = (String)tokenResponse.get("access_token");

		HttpHeaders userHeaders = new HttpHeaders();
		userHeaders.setBearerAuth(accessToken);
		HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

		Map<String, Object> userInfo;
		try {
			userInfo = restTemplate.exchange(
				GITHUB_USER_URL,
				HttpMethod.GET,
				userRequest,
				Map.class
			).getBody();
		} catch (RestClientException exception) {
			log.warn("[CliAuth] GitHub user request failed - sessionId: {}", loginSessionId, exception);
			throw new BusinessException(ErrorCode.GITHUB_OAUTH_FAILED);
		}

		if (userInfo == null || userInfo.get(GITHUB_ID_ATTRIBUTE) == null) {
			throw new BusinessException(ErrorCode.GITHUB_OAUTH_FAILED);
		}

		String githubId = String.valueOf(userInfo.get(GITHUB_ID_ATTRIBUTE));
		String email = resolveEmail(userInfo);
		session.completeAuth(githubId, email);
		redisCliSessionRepository.save(session);
	}

	@Override
	@Transactional
	public CliLoginSaveResDto registerUserAndDevice(final CliLoginSaveReqDto reqDto, final RedisCliSession session) {
		User user = findOrCreateUser(session);

		if (session.getEmail() != null
			&& !session.getEmail().isBlank()
			&& !session.getEmail().equals(user.getEmail())) {
			user.updateEmail(session.getEmail());
		}

		if (userDeviceRepository.existsByUserAndDeviceName(user, reqDto.deviceName())) {
			throw new BusinessException(ErrorCode.CLI_DEVICE_ALREADY_EXISTS);
		}

		UserDevice userDevice = CliAuthConverter.toUserDevice(reqDto, user);
		userDeviceRepository.save(userDevice);

		log.info("[CliAuth] user device saved - githubId: {}, deviceName: {}",
			session.getGithubId(), reqDto.deviceName());

		return CliAuthConverter.toLoginSaveResDto(user);
	}

	private User findOrCreateUser(final RedisCliSession session) {
		return userRepository.findByGithubId(session.getGithubId())
			.orElseGet(() -> saveOrFindUser(session));
	}

	private User saveOrFindUser(final RedisCliSession session) {
		try {
			return userRepository.save(CliAuthConverter.toUser(session));
		} catch (DataIntegrityViolationException exception) {
			return userRepository.findByGithubId(session.getGithubId())
				.orElseThrow(() -> exception);
		}
	}

	private String resolveEmail(final Map<String, Object> userInfo) {
		Object email = userInfo.get(GITHUB_EMAIL_ATTRIBUTE);
		if (email instanceof String emailValue && !emailValue.isBlank()) {
			return emailValue;
		}

		return userInfo.get(GITHUB_LOGIN_ATTRIBUTE) + "@users.noreply.github.com";
	}
}
