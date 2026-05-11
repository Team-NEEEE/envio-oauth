package io.envio.auth.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserDevice;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

	boolean existsByUserAndDeviceName(final User user, final String deviceName);
}
