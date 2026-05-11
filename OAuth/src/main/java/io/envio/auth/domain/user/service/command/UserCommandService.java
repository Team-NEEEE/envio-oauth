package io.envio.auth.domain.user.service.command;

import io.envio.auth.domain.user.entity.User;

public interface UserCommandService {

	User save(final User user);
}
