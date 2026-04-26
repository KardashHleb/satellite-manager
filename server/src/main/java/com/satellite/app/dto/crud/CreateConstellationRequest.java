package com.satellite.app.dto.crud;

import jakarta.validation.constraints.NotBlank;

public record CreateConstellationRequest(
        @NotBlank String constellationName
) {
}
