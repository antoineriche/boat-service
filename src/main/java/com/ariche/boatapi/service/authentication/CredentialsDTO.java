package com.ariche.boatapi.service.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CredentialsDTO(
    @NotBlank(message = "{user.validation.error.login.blank}")
    @Size(min = 3, max = 50, message = "{user.validation.error.login.invalid.length}")
    @Schema(name = "login", description = "Login of the user", requiredMode = Schema.RequiredMode.REQUIRED)
    String login,

    @NotBlank(message = "{user.validation.error.password.blank}")
    @Size(min = 4, max = 50, message = "{user.validation.error.password.invalid.length}")
    @Schema(name = "password", description = "Password of the user", requiredMode = Schema.RequiredMode.REQUIRED)
    String password) {
}
