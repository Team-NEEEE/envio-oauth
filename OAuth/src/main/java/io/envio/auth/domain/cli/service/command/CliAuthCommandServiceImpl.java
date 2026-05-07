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

import io.envio.auth.domain.cli.dto.request.CliLoginSaveReqDto;
import io.envio.auth.domain.cli.dto.response.CliLoginSaveResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStartResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStatusResDto;
import io.envio.auth.domain.cli.entity.RedisCliSession;
import io.envio.auth.domain.cli.entity.Role;
import io.envio.auth.domain.cli.entity.User;
import io.envio.auth.domain.cli.entity.UserDevice; // 기기 엔티티 경로에 맞게 수정 필요
import io.envio.auth.domain.cli.repository.RedisCliSessionRepository;
import io.envio.auth.domain.cli.repository.UserDeviceRepository; // 기기 레포 경로에 맞게 수정 필요
import io.envio.auth.domain.cli.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliAuthCommandServiceImpl implements CliAuthCommandService {

	// 필요한 리포지토리들 모두 주입
	private final RedisCliSessionRepository redisCliSessionRepository;
	private final UserRepository userRepository;
	private final UserDeviceRepository userDeviceRepository;

	private final RestTemplate restTemplate = new RestTemplate();

	// 환경변수 값들
	@Value("${spring.security.oauth2.client.registration.github.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.github.client-secret}")
	private String clientSecret;

	@Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
	private String redirectUri;

	private static final int EXPIRES_IN = 300;

	// --------------------------------------------------------
	// 1. [001번] 로그인 세션 생성
	// --------------------------------------------------------
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
		log.info("[CliAuth] 001 세션 생성 완료 - sessionId: {}", sessionId);

		return CliLoginStartResDto.builder()
			.loginSessionId(sessionId)
			.loginUrl(authUrl)
			.build();
	}

	// --------------------------------------------------------
	// 2. [GitHub Redirect] 토큰 발급 및 정보 저장
	// --------------------------------------------------------
	@Override
	public void processGithubCallback(String code, String sessionId) {
		log.info("[CliAuth] GitHub 콜백 수신 - code: {}, state: {}", code, sessionId);

		String tokenUrl = "https://github.com/login/oauth/access_token";
		Map<String, String> tokenParams = new HashMap<>();
		tokenParams.put("client_id", clientId);
		tokenParams.put("client_secret", clientSecret);
		tokenParams.put("code", code);
		tokenParams.put("redirect_uri", redirectUri);

		HttpHeaders tokenHeaders = new HttpHeaders();
		tokenHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);

		Map<String, Object> tokenResponse = restTemplate.postForObject(tokenUrl, tokenRequest, Map.class);
		String accessToken = (String) tokenResponse.get("access_token");

		if (accessToken == null) throw new RuntimeException("GitHub 액세스 토큰 발급 실패!");

		String userUrl = "https://api.github.com/user";
		HttpHeaders userHeaders = new HttpHeaders();
		userHeaders.setBearerAuth(accessToken);
		HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

		Map<String, Object> userInfo = restTemplate.exchange(userUrl, HttpMethod.GET, userRequest, Map.class).getBody();
		String githubId = String.valueOf(userInfo.get("login"));
		String email = (String) userInfo.get("email");

		RedisCliSession session = redisCliSessionRepository.findById(sessionId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 세션입니다."));

		// Redis 엔티티에 유저 정보 저장 및 상태 업데이트 메서드 호출
		session.completeAuth(githubId, email);
		redisCliSessionRepository.save(session);
	}

	// --------------------------------------------------------
	// 3. [002번] 터미널 폴링 상태 반환
	// --------------------------------------------------------
	@Override
	@Transactional(readOnly = true)
	public CliLoginStatusResDto getLoginStatus(String loginSessionId) {
		RedisCliSession session = redisCliSessionRepository.findById(loginSessionId)
			.orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

		if ("PENDING".equals(session.getStatus())) {
			return CliLoginStatusResDto.builder()
				.status("PENDING")
				.build();
		}

		return CliLoginStatusResDto.builder()
			.status("SUCCESS")
			.githubId(session.getGithubId())
			.email(session.getEmail())
			.build();
	}

	// --------------------------------------------------------
	// 4. [기존 003번] 실제 DB에 유저 및 기기 저장
	// --------------------------------------------------------
	@Override
	public CliLoginSaveResDto registerUserAndDevice(final CliLoginSaveReqDto reqDto) {
		User user = userRepository.findByGithubId(reqDto.githubId())
			.orElseGet(() -> {
				User newUser = User.builder()
					.githubId(reqDto.githubId())
					.email(reqDto.email())
					.role(Role.MEMBER)
					.build();
				return userRepository.save(newUser);
			});

		if (reqDto.email() != null && !reqDto.email().equals(user.getEmail())) {
			user.updateEmail(reqDto.email());
		}

		UserDevice userDevice = UserDevice.builder()
			.user(user)
			.deviceName(reqDto.deviceName())
			.publicKey(reqDto.publicKey())
			.build();

		userDeviceRepository.save(userDevice);

		log.info("[CliAuth] DB 저장 완료 - githubId: {}, deviceName: {}", reqDto.githubId(), reqDto.deviceName());

		return CliLoginSaveResDto.builder()
			.userId(user.getId())
			.deviceId(userDevice.getId())
			.githubId(user.getGithubId())
			.email(user.getEmail())
			.build();
	}

	// --------------------------------------------------------
	// 5. 사용 완료된 Redis 세션 날리기
	// --------------------------------------------------------
	@Override
	public void deleteSession(final String loginSessionId) {
		redisCliSessionRepository.deleteById(loginSessionId);
		log.info("[CliAuth] 세션 삭제 완료 - sessionId: {}", loginSessionId);
	}
}