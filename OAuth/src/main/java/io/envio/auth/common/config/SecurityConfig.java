package io.envio.auth.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import io.envio.auth.common.config.properties.CorsProperties;
import io.envio.auth.common.config.properties.OAuth2UriProperties;
import io.envio.auth.common.security.SecurityConstants;
import io.envio.auth.common.security.jwt.JwtAuthenticationEntryPoint;
import io.envio.auth.common.security.jwt.JwtAuthenticationFilter;
import io.envio.auth.common.security.oauth.CookieOAuth2AuthorizationRequestRepository;
import io.envio.auth.common.security.oauth.GitHubOAuth2UserService;
import io.envio.auth.common.security.oauth.OAuth2AuthenticationFailureHandler;
import io.envio.auth.common.security.oauth.OAuth2AuthenticationSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final CookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository;
	private final GitHubOAuth2UserService gitHubOAuth2UserService;
	private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
	private final OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;
	private final OAuth2UriProperties oauth2UriProperties;
	private final CorsProperties corsProperties;

	@Bean
	public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
		return http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(SecurityConstants.PUBLIC_URLS).permitAll()
				.requestMatchers(SecurityConstants.AUTHENTICATED_URLS).authenticated()
				.anyRequest().authenticated()
			)
			.oauth2Login(oauth -> oauth
				.authorizationEndpoint(endpoint -> endpoint
					.baseUri(oauth2UriProperties.authorizationBaseUri())
					.authorizationRequestRepository(cookieOAuth2AuthorizationRequestRepository)
				)
				.redirectionEndpoint(endpoint -> endpoint
					.baseUri(oauth2UriProperties.redirectionBaseUri())
				)
				.userInfoEndpoint(userInfo -> userInfo
					.userService(gitHubOAuth2UserService)
				)
				.successHandler(oauth2AuthenticationSuccessHandler)
				.failureHandler(oauth2AuthenticationFailureHandler)
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(corsProperties.allowedOrigins());
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
