package com.wildlifedb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI wildlifeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wildlife Reporter API")
                        .description(
                                "REST API for user authentication, wildlife observations, "
                                        + "and taxonomy lookup.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Wildlife Reporter")
                                .url("https://github.com/noko33/Wildlife_Reporter"))
                        .license(new License()
                                .name("Project repository")
                                .url("https://github.com/noko33/Wildlife_Reporter")))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_AUTH,
                                new SecurityScheme()
                                        .name(BEARER_AUTH)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "JWT returned by POST /auth/login or "
                                                        + "POST /auth/register")));
    }
}
