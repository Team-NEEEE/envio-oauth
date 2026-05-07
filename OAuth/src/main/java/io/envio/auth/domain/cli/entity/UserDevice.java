package io.envio.auth.domain.cli.entity;

import io.envio.auth.common.entity.BaseEntity;
import io.envio.auth.domain.cli.entity.User; // User 엔티티 경로에 맞게 수정 필요

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "user_devices")
public class UserDevice extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 지연 로딩(LAZY)을 사용하여 불필요한 User 조회를 방지
	// ManyToOne 기본 값 EAGER
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, updatable = false)
	private User user;

	@Column(name = "device_name", length = 255, nullable = false)
	private String deviceName;

	@Column(name = "public_key", columnDefinition = "TEXT", nullable = false)
	private String publicKey;

	// 비즈니스 로직 (Setter 대신 상태 변경용)
	public void updateDeviceName(String newDeviceName) {
		this.deviceName = newDeviceName;
	}
}