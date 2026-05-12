package io.envio.auth.domain.user.service.query;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserDevice;
import io.envio.auth.domain.user.repository.UserDeviceRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserDeviceQueryServiceImpl implements UserDeviceQueryService {

	private final UserDeviceRepository userDeviceRepository;

	@Override
	public Optional<UserDevice> findLatestByUser(final User user) {
		return userDeviceRepository.findFirstByUserOrderByCreatedAtDesc(user);
	}
}
