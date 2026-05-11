package io.envio.auth.domain.cli.service.facade;

import org.springframework.stereotype.Service;

import io.envio.auth.domain.cli.converter.CliAuthConverter;
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
		return commandService.createLoginSession();
	}

	@Override
	public void processGithubCallback(final String code, final String loginSessionId) {
		commandService.processGithubCallback(code, loginSessionId);
	}

	@Override
	public CliLoginStatusResDto getLoginStatus(final CliLoginStatusReqDto reqDto) {
		RedisCliSession session = queryService.getSession(reqDto.loginSessionId());

		return CliAuthConverter.toLoginStatusResDto(session);
	}

	@Override
	public CliLoginSaveResDto saveCliUser(final CliLoginSaveReqDto reqDto) {
		RedisCliSession session = queryService.validateSessionForSave(reqDto.loginSessionId(), reqDto.githubId());
		CliLoginSaveResDto response = commandService.registerUserAndDevice(reqDto, session);
		commandService.deleteSession(reqDto.loginSessionId());
		return response;
	}
}
