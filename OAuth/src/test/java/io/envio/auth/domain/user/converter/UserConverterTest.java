package io.envio.auth.domain.user.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.envio.auth.domain.user.dto.request.UserCreateReqDto;
import io.envio.auth.domain.user.dto.response.UserResDto;
import io.envio.auth.domain.user.entity.User;

@DisplayName("사용자_변환기")
class UserConverterTest {

	@Test
	@DisplayName("사용자_생성_요청을_엔티티로_변환한다")
	void toEntity_mapsCreateRequest() {
		// given
		UserCreateReqDto reqDto = UserCreateReqDto.builder()
			.employeeNumber("EMP001")
			.name("홍길동")
			.email("hong@example.com")
			.build();

		// when
		User user = UserConverter.toEntity(reqDto);

		// then
		assertEquals("EMP001", user.getEmployeeNumber());
		assertEquals("홍길동", user.getName());
		assertEquals("hong@example.com", user.getEmail());
	}

	@Test
	@DisplayName("사용자를_응답_DTO로_변환한다")
	void toUserResDto_mapsUser() {
		// given
		User user = User.builder()
			.id(1L)
			.employeeNumber("EMP001")
			.name("홍길동")
			.email("hong@example.com")
			.build();

		// when
		UserResDto resDto = UserConverter.toUserResDto(user);

		// then
		assertEquals(1L, resDto.userId());
		assertEquals("EMP001", resDto.employeeNumber());
		assertEquals("홍길동", resDto.name());
		assertEquals("hong@example.com", resDto.email());
	}
}
