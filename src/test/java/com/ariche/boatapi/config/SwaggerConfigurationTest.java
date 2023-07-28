package com.ariche.boatapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class SwaggerConfigurationTest {

    @Test
    void test_BoatApiCustomizer() {
        final OpenApiCustomizer res = new SwaggerConfiguration().boatApiCustomizer();
        final OpenAPI openAPI = spy(new OpenAPI()
            .components(new Components()));
        res.customise(openAPI);
        verify(openAPI).info(any(Info.class));
        assertEquals("BOAT API", openAPI.getInfo().getTitle());
        assertFalse(openAPI.getComponents().getSecuritySchemes().isEmpty());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey(HttpHeaders.AUTHORIZATION));

        final SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get(HttpHeaders.AUTHORIZATION);
        assertEquals(SecurityScheme.In.HEADER, securityScheme.getIn());
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
    }
}
