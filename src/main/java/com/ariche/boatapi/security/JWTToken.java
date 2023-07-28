package com.ariche.boatapi.security;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record JWTToken(
    @NotBlank
    @Schema(name = "accessToken", description = "The access token", requiredMode = Schema.RequiredMode.REQUIRED)
    String accessToken,

    @Min(value = 0)
    @Schema(name = "expiresIn", description = "The time before expiration (in seconds)", example = "300", requiredMode = Schema.RequiredMode.REQUIRED)
    Long expiredIn,

    @Schema(name = "tokenType", description = "The token type", example = "Bearer", requiredMode = Schema.RequiredMode.REQUIRED)
    String tokenType) { }
