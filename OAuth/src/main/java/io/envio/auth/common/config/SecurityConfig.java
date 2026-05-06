package io.envio.auth.common.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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

import io.envio.auth.common.security.SecurityConstants;
import io.envio.auth.common.security.jwt.JwtAuthenticationFilter;
import io.envio.auth.common.security.oauth.CookieOAuth2AuthorizationRequestRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String[] AUTHENTICATED_URLS = {
		"/api/auth/me",
		"/api/auth/logout",
		"/api/auth/projects/**"
	};

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository;
	private final List<String> allowedOrigins;

	public SecurityConfig(
		final JwtAuthenticationFilter jwtAuthenticationFilter,
		final CookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository,
		@Value("${cors.allowed-origins}") final List<String> allowedOrigins
	) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.cookieOAuth2AuthorizationRequestRepository = cookieOAuth2AuthorizationRequestRepository;
		this.allowedOrigins = allowedOrigins;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
		return http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(SecurityConstants.PUBLIC_URLS).permitAll()
				.requestMatchers(AUTHENTICATED_URLS).authenticated()
				.anyRequest().authenticated()
			)
			.oauth2Login(oauth -> oauth
				.authorizationEndpoint(endpoint -> endpoint
					.authorizationRequestRepository(cookieOAuth2AuthorizationRequestRepository)
				)
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(allowedOrigins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
