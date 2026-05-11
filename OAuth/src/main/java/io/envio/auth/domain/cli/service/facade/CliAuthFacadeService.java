package io.envio.auth.domain.cli.service.facade;

import io.envio.auth.domain.cli.dto.request.CliLoginSaveReqDto;
import io.envio.auth.domain.cli.dto.request.CliLoginStatusReqDto;
import io.envio.auth.domain.cli.dto.response.CliLoginSaveResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStartResDto;
import io.envio.auth.domain.cli.dto.response.CliLoginStatusResDto;

public interface CliAuthFacadeService {
	CliLoginStartResDto startLogin();

	void processGithubCallback(final String code, final String loginSessionId);

	CliLoginStatusResDto getLoginStatus(final CliLoginStatusReqDto reqDto);

	CliLoginSaveResDto saveCliUser(final CliLoginSaveReqDto reqDto);
}
