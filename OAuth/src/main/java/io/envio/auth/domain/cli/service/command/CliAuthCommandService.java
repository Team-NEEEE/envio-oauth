package io.envio.auth.domain.cli.service.command;

import io.envio.auth.domain.cli.dto.request.CliLoginSaveReqDto;
import io.envio.auth.domain.cli.dto.response.CliLoginSaveResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStartResDto;
import io.envio.auth.domain.cli.entity.RedisCliSession;

public interface CliAuthCommandService {

	CliLoginStartResDto createLoginSession();

	void processGithubCallback(final String code, final String loginSessionId);

	CliLoginSaveResDto registerUserAndDevice(final CliLoginSaveReqDto reqDto, final RedisCliSession session);
}
