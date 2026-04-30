package io.envio.auth.domain.user.entity;

import io.envio.auth.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	uniqueConstraints = @UniqueConstraint(name = "uk_users_employee_number", columnNames = "employee_number")
)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "employee_number", unique = true, length = 100, nullable = false)
	private String employeeNumber;

	@Column(name = "name", length = 50, nullable = false)
	private String name;

	@Column(name = "email", nullable = false)
	private String email;
}
