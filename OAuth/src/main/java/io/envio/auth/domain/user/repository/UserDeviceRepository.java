package io.envio.auth.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.auth.domain.user.entity.UserDevice;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
}
