package io.envio.auth.common.security.jwt;

public class JwtParsingException extends RuntimeException {

	public JwtParsingException(final String message) {
		super(message);
	}

	public JwtParsingException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
