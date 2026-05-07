package io.envio.auth.domain.cli.service.facade;

import org.springframework.stereotype.Service;

import io.envio.auth.domain.cli.dto.request.CliLoginSaveReqDto;
import io.envio.auth.domain.cli.dto.request.CliLoginStatusReqDto;
import io.envio.auth.domain.cli.dto.response.CliLoginSaveResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStartResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStatusResDto;
import io.envio.auth.domain.cli.entity.RedisCliSession;
import io.envio.auth.domain.cli.service.command.CliAuthCommandService;
import io.envio.auth.domain.cli.service.query.CliAuthQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliAuthFacadeServiceImpl implements CliAuthFacadeService {

	private final CliAuthCommandService commandService;
	private final CliAuthQueryService queryService;

	@Override
	public CliLoginStartResDto startLogin() {
		// [AUTH_CLI_001] 세션 생성
		return commandService.createLoginSession();
	}

	@Override
	public CliLoginStatusResDto getLoginStatus(final CliLoginStatusReqDto reqDto) {
		// [AUTH_CLI_002] 상태 조회 (Body에서 ID 추출)
		RedisCliSession session = queryService.getSession(reqDto.loginSessionId());

		return CliLoginStatusResDto.builder()
			.status(session.getStatus())
			.githubId(session.getGithubId())
			.email(session.getEmail())
			.build();
	}

	@Override
	public CliLoginSaveResDto saveCliUser(final CliLoginSaveReqDto reqDto) {
		// [AUTH_CLI_003] 최종 유저 및 기기 저장
		// 1. 세션이 SUCCESS 상태인지, 요청한 유저가 맞는지 검증
		queryService.validateSessionForSave(reqDto.loginSessionId(), reqDto.githubId());

		// 2. DB에 데이터 쓰기 (User, UserDevice)
		CliLoginSaveResDto resDto = commandService.registerUserAndDevice(reqDto);

		// 3. 목적을 달성한 1회용 세션 삭제
		commandService.deleteSession(reqDto.loginSessionId());

		return resDto;
	}
}