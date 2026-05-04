package io.envio.auth.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.auth.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmployeeNumber(String employeeNumber);

	boolean existsByEmployeeNumber(String employeeNumber);
}
