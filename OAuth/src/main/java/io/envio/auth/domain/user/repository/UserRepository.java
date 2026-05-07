package io.envio.auth.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.auth.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByGithubId(final String githubId);
}
