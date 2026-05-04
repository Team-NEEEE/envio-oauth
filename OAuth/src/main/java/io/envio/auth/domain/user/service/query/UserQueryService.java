package io.envio.auth.domain.user.service.query;

import io.envio.auth.domain.user.entity.User;

public interface UserQueryService {

	User findById(final Long userId);

	User findByEmployeeNumber(final String employeeNumber);
}
