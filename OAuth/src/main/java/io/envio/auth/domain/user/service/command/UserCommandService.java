package io.envio.auth.domain.user.service.command;

import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserRole;

public interface UserCommandService {

	User save(final User user);

	User updateRole(final User user, final UserRole role);
}
