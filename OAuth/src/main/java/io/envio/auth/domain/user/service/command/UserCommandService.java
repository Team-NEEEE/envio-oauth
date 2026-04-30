package io.envio.auth.domain.user.service.command;

import io.envio.auth.domain.user.dto.request.UserCreateReqDto;
import io.envio.auth.domain.user.entity.User;

public interface UserCommandService {

	User create(final UserCreateReqDto reqDto);
}
