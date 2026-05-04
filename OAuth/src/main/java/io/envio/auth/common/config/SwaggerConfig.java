package io.envio.auth.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenApi() {

		return new OpenAPI()
			.info(new Info().title("Envio Auth API")
				.description("Envio Auth API Server")
				.version("v1.0"));
	}
}