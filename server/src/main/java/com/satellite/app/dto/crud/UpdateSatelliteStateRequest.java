package com.satellite.app.dto.crud;

import jakarta.validation.constraints.NotNull;

public record UpdateSatelliteStateRequest(
        @NotNull Boolean active
) {
}
