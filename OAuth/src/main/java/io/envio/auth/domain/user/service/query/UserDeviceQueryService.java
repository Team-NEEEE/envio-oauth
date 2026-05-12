package io.envio.auth.domain.user.service.query;

import java.util.Optional;

import io.envio.auth.domain.user.entity.UserDevice;

public interface UserDeviceQueryService {

	Optional<UserDevice> findLatestByUserId(final Long userId);
}
