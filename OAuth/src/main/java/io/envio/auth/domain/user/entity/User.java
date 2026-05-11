package io.envio.auth.domain.user.entity;

import io.envio.auth.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "users",
	uniqueConstraints = @UniqueConstraint(name = "uk_users_github_id", columnNames = "user_github_id")
)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(name = "user_github_id", unique = true, nullable = false, length = 255)
	private String githubId;

	@Column(name = "email", nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private UserRole role;

	public static User createGithubUser(final String githubId, final String email) {
		return User.builder()
			.githubId(githubId)
			.email(email)
			.role(UserRole.VIEWER)
			.build();
	}

	public void updateEmail(final String email) {
		this.email = email;
	}
}
