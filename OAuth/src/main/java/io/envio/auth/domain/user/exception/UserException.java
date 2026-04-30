package io.envio.auth.domain.user.exception;

import io.envio.auth.common.error.ErrorCode;
import io.envio.auth.common.error.exception.BusinessException;

public class UserException extends BusinessException {

	public UserException(final ErrorCode errorCode) {
		super(errorCode);
	}
}
