package io.envio.auth.domain.user.service.facade;

import io.envio.auth.domain.user.dto.request.UserCreateReqDto;
import io.envio.auth.domain.user.dto.response.UserResDto;

public interface UserFacadeService {

	UserResDto createUser(final UserCreateReqDto reqDto);

	UserResDto getUser(final Long userId);

	UserResDto getUserByGithubId(final String githubId);
}
