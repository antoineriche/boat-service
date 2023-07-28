package com.ariche.boatapi.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("Public")
            .pathsToMatch("/auth/token")
            .build();
    }

    @Bean
    public GroupedOpenApi boatApi() {
        return GroupedOpenApi.builder()
            .group("Boat")
            .packagesToScan("com.ariche.boatapi")
            .addOpenApiCustomizer(boatApiCustomizer())
            .pathsToMatch("/api/v1/boats/**")
            .build();
    }

    @Bean
    public OpenApiCustomizer boatApiCustomizer() {
        return openApi -> openApi
            .info(new Info().title("BOAT API"))
            .getComponents().addSecuritySchemes("Authorization", new SecurityScheme()
                .in(SecurityScheme.In.HEADER)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"));
    }

}
