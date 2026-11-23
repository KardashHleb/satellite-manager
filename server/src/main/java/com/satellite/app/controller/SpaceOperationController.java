package com.satellite.app.controller;

import com.satellite.app.dto.MissionRequest;
import com.satellite.app.exception.ResourceNotFoundException;
import com.satellite.app.exception.SpaceOperationException;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.service.SpaceOperationCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SpaceOperationController {

    private static final Logger log = LoggerFactory.getLogger(SpaceOperationController.class);

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

    // Add a satellite (using the AddSatelliteRequest DTO)
    @PostMapping("/add-satellites")
    public ResponseEntity<String> addSatellite(@RequestBody com.satellite.app.service.AddSatelliteRequest request) {
        try {
            spaceOperationCenterService.addSatellite(request);
            return ResponseEntity.ok("Satellite added to constellation " + request.getConstellationName());
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Execute a mission
    @PostMapping("/missions")
    public ResponseEntity<String> executeMission(@RequestBody MissionRequest request) {
        try {
            spaceOperationCenterService.executeMission(request);
            if (request.getSatelliteName() != null && !request.getSatelliteName().isBlank()) {
                return ResponseEntity.ok("Mission for satellite " + request.getSatelliteName() + " completed");
            }
            return ResponseEntity.ok("Mission for constellation " + request.getConstellationName() + " completed");
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
            var satellite = spaceOperationCenterService.findSatellite(constellationName, satelliteName);
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
            var satellite = spaceOperationCenterService.findSatellite(constellationName, satelliteName);
            spaceOperationCenterService.deactivateSatellite(constellationName, satellite);
            return ResponseEntity.ok("Satellite " + satelliteName + " deactivated");
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Text overview
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
            var satellite = spaceOperationCenterService.findSatellite(constellationName, satelliteName);
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
            var satellite = spaceOperationCenterService.findSatellite(constellationName, satelliteName);
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

    /**
     * 404 when a constellation or satellite does not exist, 400 for invalid input, 500 for anything else.
     */
    private static ResponseEntity<String> errorResponse(Exception e) {
        if (e instanceof ResourceNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
        if (e instanceof IllegalArgumentException || e instanceof SpaceOperationException) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
        log.error("Space operation failed", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: internal server error");
    }
}
