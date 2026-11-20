package com.satellite.app.controller;

import com.satellite.app.dto.MissionRequest;
import com.satellite.app.exception.NotFoundException;
import com.satellite.app.exception.SpaceOperationException;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.service.AddSatelliteRequest;
import com.satellite.app.service.SpaceOperationCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SpaceOperationController {

    private final SpaceOperationCenterService spaceOperationCenterService;

    @Autowired
    public SpaceOperationController(SpaceOperationCenterService spaceOperationCenterService) {
        this.spaceOperationCenterService = spaceOperationCenterService;
    }

    // Get all constellations
    @GetMapping("/constellations")
    public ResponseEntity<Map<String, SatelliteConstellation>> getAllConstellations() {
        return ResponseEntity.ok(spaceOperationCenterService.getAll());
    }

    // Get one constellation
    @GetMapping("/constellations/{constellationName}")
    public ResponseEntity<SatelliteConstellation> getConstellation(@PathVariable String constellationName) {
        return spaceOperationCenterService.get(constellationName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add a satellite (the constellation is created when missing)
    @PostMapping("/add-satellites")
    public ResponseEntity<String> addSatellite(@RequestBody AddSatelliteRequest request) {
        try {
            if (request.getSatelliteParam() == null) {
                throw new IllegalArgumentException("satelliteParam is required");
            }
            spaceOperationCenterService.addSatellite(request);
            return ResponseEntity.ok("Satellite added to constellation " + request.getConstellationName());
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Run a mission
    @PostMapping("/missions")
    public ResponseEntity<String> executeMission(@RequestBody MissionRequest request) {
        try {
            if ("CONSTELLATION".equals(request.getTargetType())) {
                spaceOperationCenterService.executeMissions(request.getConstellationName());
                return ResponseEntity.ok("Mission for constellation " + request.getConstellationName() + " completed");
            } else if ("SINGLE_SATELLITE".equals(request.getTargetType())) {
                var satellite = findSatellite(request.getConstellationName(), request.getSatelliteName());
                spaceOperationCenterService.performSatelliteMission(
                        request.getConstellationName(),
                        satellite
                );
                return ResponseEntity.ok("Mission for satellite " + request.getSatelliteName() + " completed");
            } else {
                throw new IllegalArgumentException("Unknown target type: " + request.getTargetType());
            }
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Activate a satellite
    @PostMapping("/constellations/{constellationName}/satellites/{satelliteName}/activate")
    public ResponseEntity<String> activateSatellite(
            @PathVariable String constellationName,
            @PathVariable String satelliteName) {

        try {
            var satellite = findSatellite(constellationName, satelliteName);
            spaceOperationCenterService.activateSatellite(constellationName, satellite);
            return ResponseEntity.ok("Satellite " + satelliteName + " activated");
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Deactivate a satellite
    @PostMapping("/constellations/{constellationName}/satellites/{satelliteName}/deactivate")
    public ResponseEntity<String> deactivateSatellite(
            @PathVariable String constellationName,
            @PathVariable String satelliteName) {

        try {
            var satellite = findSatellite(constellationName, satelliteName);
            spaceOperationCenterService.deactivateSatellite(constellationName, satellite);
            return ResponseEntity.ok("Satellite " + satelliteName + " deactivated");
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Get a text overview
    @GetMapping("/overview")
    public ResponseEntity<String> getOverview() {
        StringBuilder overview = new StringBuilder("=== Space Operation Center Overview ===\n\n");

        var constellations = spaceOperationCenterService.getAll();
        for (var entry : constellations.entrySet()) {
            overview.append(entry.getValue().getStatusReport());
            overview.append("\n");
        }

        return ResponseEntity.ok(overview.toString());
    }

    // Take a photo (imaging satellites only)
    @PostMapping("/constellations/{constellationName}/satellites/{satelliteName}/photo")
    public ResponseEntity<String> takePhoto(
            @PathVariable String constellationName,
            @PathVariable String satelliteName) {

        try {
            var satellite = findSatellite(constellationName, satelliteName);
            if (satellite instanceof ImagingSatellite imagingSat) {
                spaceOperationCenterService.takePhoto(constellationName, imagingSat);
                return ResponseEntity.ok("Photo taken by satellite " + satelliteName);
            } else {
                return ResponseEntity.badRequest().body("Error: satellite " + satelliteName + " is not an imaging satellite");
            }
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Send data (communication satellites only)
    @PostMapping("/constellations/{constellationName}/satellites/{satelliteName}/send-data")
    public ResponseEntity<String> sendData(
            @PathVariable String constellationName,
            @PathVariable String satelliteName,
            @RequestParam double dataSize) {

        try {
            var satellite = findSatellite(constellationName, satelliteName);
            if (satellite instanceof CommunicationSatellite commSat) {
                spaceOperationCenterService.sendData(constellationName, commSat, dataSize);
                return ResponseEntity.ok("Sent " + dataSize + " GB of data via satellite " + satelliteName);
            } else {
                return ResponseEntity.badRequest().body("Error: satellite " + satelliteName + " is not a communication satellite");
            }
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Helper: find a satellite by name in a constellation
    private Satellite findSatellite(String constellationName, String satelliteName) {
        return spaceOperationCenterService.getSatellites(constellationName)
                .stream()
                .filter(s -> s.getName().equals(satelliteName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Satellite not found: " + satelliteName + " in constellation: " + constellationName
                ));
    }

    // Helper: 404 for a missing constellation or satellite, 400 for invalid input, 500 for anything else
    private ResponseEntity<String> errorResponse(Exception e) {
        if (e instanceof NotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
        if (e instanceof IllegalArgumentException || e instanceof SpaceOperationException) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
    }
}
