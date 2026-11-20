package com.satellite.app.dto.crud;

import jakarta.validation.constraints.NotBlank;

public record RenameConstellationRequest(
        @NotBlank String newName
) {
}
