package io.envio.auth.domain.user.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.envio.auth.domain.user.dto.request.UserCreateReqDto;
import io.envio.auth.domain.user.dto.response.UserResDto;
import io.envio.auth.domain.user.entity.User;
import io.envio.auth.domain.user.entity.UserRole;

@DisplayName("사용자 변환기")
class UserConverterTest {

	@Test
	@DisplayName("사용자 생성 요청을 엔티티로 변환한다")
	void toEntityMapsCreateRequest() {
		UserCreateReqDto reqDto = UserCreateReqDto.builder()
			.githubId("123456")
			.email("user@example.com")
			.build();

		User user = UserConverter.toEntity(reqDto);

		assertEquals("123456", user.getGithubId());
		assertEquals("user@example.com", user.getEmail());
		assertEquals(UserRole.VIEWER, user.getRole());
	}

	@Test
	@DisplayName("사용자를 응답 DTO로 변환한다")
	void toUserResDtoMapsUser() {
		User user = User.builder()
			.id(1L)
			.githubId("123456")
			.email("user@example.com")
			.role(UserRole.VIEWER)
			.build();

		UserResDto resDto = UserConverter.toUserResDto(user);

		assertEquals(1L, resDto.userId());
		assertEquals("123456", resDto.githubId());
		assertEquals("user@example.com", resDto.email());
		assertEquals("VIEWER", resDto.role());
	}
}
