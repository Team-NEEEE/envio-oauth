package io.envio.auth.common.security.oauth;

import java.util.Map;
import java.util.Set;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthorizationRequestSerializer {

	private final ObjectMapper objectMapper;

	public String serialize(final OAuth2AuthorizationRequest authorizationRequest) {
		AuthorizationRequestPayload payload = AuthorizationRequestPayload.from(authorizationRequest);
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize OAuth2 authorization request.", exception);
		}
	}

	public OAuth2AuthorizationRequest deserialize(final String authorizationRequestJson) {
		try {
			return objectMapper.readValue(authorizationRequestJson, AuthorizationRequestPayload.class)
				.toAuthorizationRequest();
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to deserialize OAuth2 authorization request.", exception);
		}
	}

	private record AuthorizationRequestPayload(
		String authorizationUri,
		String clientId,
		String redirectUri,
		Set<String> scopes,
		String state,
		Map<String, Object> additionalParameters,
		String authorizationRequestUri,
		Map<String, Object> attributes
	) {

		private static AuthorizationRequestPayload from(final OAuth2AuthorizationRequest authorizationRequest) {
			return new AuthorizationRequestPayload(
				authorizationRequest.getAuthorizationUri(),
				authorizationRequest.getClientId(),
				authorizationRequest.getRedirectUri(),
				authorizationRequest.getScopes(),
				authorizationRequest.getState(),
				authorizationRequest.getAdditionalParameters(),
				authorizationRequest.getAuthorizationRequestUri(),
				authorizationRequest.getAttributes()
			);
		}

		private OAuth2AuthorizationRequest toAuthorizationRequest() {
			return OAuth2AuthorizationRequest.authorizationCode()
				.authorizationUri(authorizationUri)
				.clientId(clientId)
				.redirectUri(redirectUri)
				.scopes(scopes)
				.state(state)
				.additionalParameters(additionalParameters)
				.authorizationRequestUri(authorizationRequestUri)
				.attributes(attributes)
				.build();
		}
	}
}
