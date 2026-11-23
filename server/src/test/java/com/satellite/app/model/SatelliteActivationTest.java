package com.satellite.app.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Satellite activation")
class SatelliteActivationTest {

    @Test
    @DisplayName("activate turns on a charged satellite and reports false when it is already active")
    void activate_ChargedSatellite_ThenAlreadyActive() {
        Satellite satellite = new ImagingSatellite("Imager-T", 0.9, 1.0);

        assertTrue(satellite.activate());
        assertFalse(satellite.activate(), "A second activation should not count as a new one");
        assertTrue(satellite.isActive());
    }

    @Test
    @DisplayName("activate does not turn on a satellite with a low battery")
    void activate_LowBattery_StaysOff() {
        Satellite satellite = new CommunicationSatellite("Comm-T", 0.1, 100.0);

        assertFalse(satellite.activate());
        assertFalse(satellite.isActive());
    }
}
