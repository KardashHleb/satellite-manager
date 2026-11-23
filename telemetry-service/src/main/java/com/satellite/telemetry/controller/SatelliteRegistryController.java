package com.satellite.telemetry.controller;

import com.satellite.telemetry.kafka.SatelliteRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/telemetry")
public class SatelliteRegistryController {

    private final SatelliteRegistry satelliteRegistry;

    public SatelliteRegistryController(SatelliteRegistry satelliteRegistry) {
        this.satelliteRegistry = satelliteRegistry;
    }

    /**
     * Satellites that the telemetry service learned about through Kafka (to check the integration).
     */
    @GetMapping("/known-satellites")
    public Map<String, Set<String>> knownSatellites() {
        return satelliteRegistry.snapshot();
    }
}
