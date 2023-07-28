package com.ariche.boatapi.service.boatmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoatDTO(
    @Nullable
    @Min(value = 0, message = "{validation.error.invalid.id}")
    @Schema(name = "id", description = "Id of the boat", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Long id,
    @NotBlank(message = "{boat.validation.error.name.blank}")
    @Size(min = 2, max = 100, message = "{boat.validation.error.name.invalid.length}")
    @Schema(name = "name", description = "Name of the boat", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Nullable
    @Size(max = 2_000, message = "{boat.validation.error.description.too.large}")
    @Schema(name = "description", description = "Description of the boat", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String description,

    // TODO: Could be added to DTO to enable clients to get the URL directly in the returned object
    @Nullable
    @Schema(name = "imgUrl", description = "URL of the boat image", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String imgUrl) {
    public BoatDTO(Long id, String name, String description) {
        this(id, name, description, null);
    }
}
