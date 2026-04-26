package com.satellite.app.dto.crud;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateBatteryRequest(
        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        Double batteryLevel
) {
}
