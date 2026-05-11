package io.envio.auth.common.error;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {

	/**
	 * Common Error (C-xxx)
	 */
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "C-001", "Invalid request."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "C-002", "Resource not found."),
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "C-003", "Invalid input."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C-004", "Internal server error."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "C-005", "Access denied."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C-006", "Method not allowed."),
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "C-007", "Unsupported media type."),
	DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "C-008", "Data integrity violation."),
	OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, "C-009", "Optimistic lock conflict."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C-010", "Authentication is required."),

	/**
	 * User Error (US-xxx)
	 */
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "US-001", "User not found."),
	USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "US-002", "User already exists."),

	/**
	 * CLI Auth Error (CLI-xxx)
	 */
	CLI_LOGIN_SESSION_INVALID(HttpStatus.BAD_REQUEST, "CLI-001", "Invalid or expired CLI login session."),
	CLI_LOGIN_SESSION_NOT_READY(HttpStatus.BAD_REQUEST, "CLI-002", "CLI login session is not authenticated."),
	CLI_LOGIN_SESSION_MISMATCH(HttpStatus.BAD_REQUEST, "CLI-003", "CLI login session does not match request."),
	CLI_DEVICE_ALREADY_EXISTS(HttpStatus.CONFLICT, "CLI-004", "CLI device name already exists."),
	GITHUB_OAUTH_FAILED(HttpStatus.BAD_GATEWAY, "CLI-005", "GitHub OAuth request failed."),
	CLI_LOGIN_SESSION_ALREADY_PROCESSING(
		HttpStatus.CONFLICT,
		"CLI-006",
		"CLI login session is already being processed."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
