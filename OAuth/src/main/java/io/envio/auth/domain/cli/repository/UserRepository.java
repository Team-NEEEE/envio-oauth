package io.envio.auth.domain.cli.repository; // 패키지는 user 도메인에 두는 것을 권장합니다.

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.auth.domain.cli.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	// CLI 로그인(GitHub 인증)을 위해 사용하는 메서드
	Optional<User> findByGithubId(String githubId);
}