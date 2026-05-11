package io.envio.auth.domain.cli.service.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliAuthCommandServiceImpl implements CliAuthCommandService {

	private static final int EXPIRES_IN = 300;

	private final RedisCliSessionRepository redisCliSessionRepository;
	private final UserRepository userRepository;
	private final UserDeviceRepository userDeviceRepository;

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${spring.security.oauth2.client.registration.github.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.github.client-secret}")
	private String clientSecret;

	@Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
	private String redirectUri;

	@Override
	public CliLoginStartResDto createLoginSession() {
		String sessionId = UUID.randomUUID().toString();
		String authUrl = String.format(
			"https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&state=%s&scope=read:user,user:email",
			clientId, redirectUri, sessionId
		);

		RedisCliSession session = RedisCliSession.builder()
			.id(sessionId)
			.status("PENDING")
			.expiresIn(EXPIRES_IN)
			.build();

		redisCliSessionRepository.save(session);
		log.info("[CliAuth] login session created - sessionId: {}", sessionId);

		return CliAuthConverter.toLoginStartResDto(sessionId, authUrl);
	}

	@Override
	public void processGithubCallback(final String code, final String loginSessionId) {
		log.info("[CliAuth] GitHub callback received - sessionId: {}", loginSessionId);

		Map<String, String> tokenParams = new HashMap<>();
		tokenParams.put("client_id", clientId);
		tokenParams.put("client_secret", clientSecret);
		tokenParams.put("code", code);
		tokenParams.put("redirect_uri", redirectUri);

		HttpHeaders tokenHeaders = new HttpHeaders();
		tokenHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);

		Map<String, Object> tokenResponse = restTemplate.postForObject(
			"https://github.com/login/oauth/access_token",
			tokenRequest,
			Map.class
		);
		String accessToken = (String)tokenResponse.get("access_token");

		if (accessToken == null) {
			throw new RuntimeException("GitHub access token issue failed.");
		}

		HttpHeaders userHeaders = new HttpHeaders();
		userHeaders.setBearerAuth(accessToken);
		HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

		Map<String, Object> userInfo = restTemplate.exchange(
			"https://api.github.com/user",
			HttpMethod.GET,
			userRequest,
			Map.class
		).getBody();

		String githubId = String.valueOf(userInfo.get("login"));
		String email = (String)userInfo.get("email");

		RedisCliSession session = redisCliSessionRepository.findById(loginSessionId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid or expired login session."));

		session.completeAuth(githubId, email);
		redisCliSessionRepository.save(session);
	}

	@Override
	public CliLoginSaveResDto registerUserAndDevice(final CliLoginSaveReqDto reqDto) {
		User user = userRepository.findByGithubId(reqDto.githubId())
			.orElseGet(() -> userRepository.save(CliAuthConverter.toUser(reqDto)));

		if (reqDto.email() != null && !reqDto.email().isBlank() && !reqDto.email().equals(user.getEmail())) {
			user.updateEmail(reqDto.email());
		}

		UserDevice userDevice = CliAuthConverter.toUserDevice(reqDto, user);
		userDeviceRepository.save(userDevice);

		log.info("[CliAuth] user device saved - githubId: {}, deviceName: {}", reqDto.githubId(), reqDto.deviceName());

		return CliAuthConverter.toLoginSaveResDto(user, userDevice);
	}

	@Override
	public void deleteSession(final String loginSessionId) {
		redisCliSessionRepository.deleteById(loginSessionId);
		log.info("[CliAuth] login session deleted - sessionId: {}", loginSessionId);
	}
}
