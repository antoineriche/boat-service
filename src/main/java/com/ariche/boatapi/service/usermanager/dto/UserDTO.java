package com.ariche.boatapi.service.usermanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public record UserDTO(
    @Nullable
    @Min(value = 0, message = "{validation.error.invalid.id}")
    @Schema(name = "id", description = "Id of the user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Long id,
    @NotBlank(message = "{user.validation.error.login.blank}")
    @Size(min = 3, max = 50, message = "{user.validation.error.login.invalid.length}")
    @Schema(name = "login", description = "Login of the user", requiredMode = Schema.RequiredMode.REQUIRED)
    String login,
    @Nullable
    @Schema(name = "authorities", description = "Authorities of the user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<String> authorities) {
    public UserDTO(Long id, String login) {
        this(id, login, new ArrayList<>());
    }
}
