package io.envio.auth.domain.view.service;

import org.springframework.security.core.Authentication;

import io.envio.auth.common.security.jwt.JwtClaims;
import io.envio.auth.domain.view.dto.response.AuthMeResDto;
import io.envio.auth.domain.view.dto.response.OAuthLoginResDto;

public interface ViewAuthService {

	OAuthLoginResDto issueOAuthLoginTokens(final Authentication authentication);

	AuthMeResDto getCurrentUser(final JwtClaims claims);
}
