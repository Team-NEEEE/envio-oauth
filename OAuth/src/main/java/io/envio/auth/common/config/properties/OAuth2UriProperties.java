package io.envio.auth.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.oauth2")
public record OAuth2UriProperties(
	String authorizationBaseUri,
	String redirectionBaseUri,
	String frontendRedirectUri
) {
}
