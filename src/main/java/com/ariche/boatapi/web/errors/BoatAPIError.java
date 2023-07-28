package com.ariche.boatapi.web.errors;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record BoatAPIError(
    @NotBlank
    @Schema(name = "code", description = "Unique code of the error", example = "0728151228A", requiredMode = Schema.RequiredMode.REQUIRED)
    String code,
    @NotBlank
    @Schema(name = "category", description = "Category of the error", example = "Invalid data", requiredMode = Schema.RequiredMode.REQUIRED)
    String category,
    @NotBlank
    @Schema(name = "time", description = "Time of the error (UTC)", example = "2023-07-28T13:04:27.361555", requiredMode = Schema.RequiredMode.REQUIRED)
    String time,
    @Schema(name = "problems", description = "List of problems", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<ProblemDTO> problems) {
    public BoatAPIError(String code, String category, List<ProblemDTO> problems) {
        this(code, category, LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME), problems);
    }
}
