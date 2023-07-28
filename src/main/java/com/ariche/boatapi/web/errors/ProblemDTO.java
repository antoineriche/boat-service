package com.ariche.boatapi.web.errors;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

public record ProblemDTO(
    @NotBlank
    @Schema(name = "error", description = "Message of the error", example = "You must be authenticated", requiredMode = Schema.RequiredMode.REQUIRED)
    String error,

    @Nullable
    @Schema(name = "detail", description = "Detail of the error", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String detail) {
    public ProblemDTO(String error) {
        this(error, null);
    }
}
