package io.envio.auth.domain.cli.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.auth.domain.cli.entity.UserDevice;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
}
