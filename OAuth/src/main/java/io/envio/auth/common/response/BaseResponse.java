package io.envio.auth.common.response;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record BaseResponse<T>(
	@JsonIgnore
	HttpStatus httpStatus,
	String message,
	boolean success,
	T data,
	ErrorResponse error,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN, timezone = TIMEZONE_SEOUL)
	ZonedDateTime timestamp
) {

	static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	static final String TIMEZONE_SEOUL = "Asia/Seoul";

	private static ZonedDateTime now() {
		return ZonedDateTime.now(ZoneId.of(TIMEZONE_SEOUL));
	}

	public static <T> BaseResponse<T> ok(final T data) {
		return ok("요청이 성공했습니다.", data);
	}

	public static <T> BaseResponse<T> ok(final String message, final T data) {
		return BaseResponse.<T>builder()
			.httpStatus(HttpStatus.OK)
			.message(message)
			.success(true)
			.data(data)
			.error(null)
			.timestamp(now())
			.build();
	}

	public static <T> BaseResponse<T> created(final T data) {
		return BaseResponse.<T>builder()
			.httpStatus(HttpStatus.CREATED)
			.message("요청이 성공했습니다.")
			.success(true)
			.data(data)
			.error(null)
			.timestamp(now())
			.build();
	}

	public static <T> BaseResponse<T> accepted() {
		return BaseResponse.<T>builder()
			.httpStatus(HttpStatus.ACCEPTED)
			.message("요청이 접수되었습니다.")
			.success(true)
			.data(null)
			.error(null)
			.timestamp(now())
			.build();
	}

	public static <T> BaseResponse<T> fail(@NonNull final ErrorResponse error) {
		return fail(error.message(), error);
	}

	public static <T> BaseResponse<T> fail(final String message, @NonNull final ErrorResponse error) {
		return BaseResponse.<T>builder()
			.httpStatus(error.status())
			.message(message)
			.success(false)
			.data(null)
			.error(error)
			.timestamp(now())
			.build();
	}
}
