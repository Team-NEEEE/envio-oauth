package io.envio.auth.domain.view.service;

import org.springframework.security.core.Authentication;

import io.envio.auth.common.security.jwt.JwtClaims;
import io.envio.auth.domain.view.dto.request.AuthProjectMemberRoleUpdateReqDto;
import io.envio.auth.domain.view.dto.request.AuthRefreshReqDto;
import io.envio.auth.domain.view.dto.response.AuthMeResDto;
import io.envio.auth.domain.view.dto.response.AuthProjectMemberRoleUpdateResDto;
import io.envio.auth.domain.view.dto.response.AuthRefreshResDto;
import io.envio.auth.domain.view.dto.response.OAuthLoginResDto;

public interface ViewAuthService {

	OAuthLoginResDto issueOAuthLoginTokens(final Authentication authentication);

	AuthMeResDto getCurrentUser(final JwtClaims claims);

	AuthRefreshResDto refreshToken(final AuthRefreshReqDto reqDto);

	void logout(final JwtClaims claims);

	AuthProjectMemberRoleUpdateResDto updateProjectMemberRole(
		final JwtClaims claims,
		final Long projectId,
		final Long userId,
		final AuthProjectMemberRoleUpdateReqDto reqDto
	);
}
