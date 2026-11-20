package com.satellite.app.controller;

import com.satellite.app.dto.MissionRequest;
import com.satellite.app.exception.NotFoundException;
import com.satellite.app.exception.SpaceOperationException;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.SatelliteConstellation;
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

    // Получить все группировки
    @GetMapping("/constellations")
    public ResponseEntity<Map<String, SatelliteConstellation>> getAllConstellations() {
        return ResponseEntity.ok(spaceOperationCenterService.getAll());
    }

    // Получить конкретную группировку
    @GetMapping("/constellations/{constellationName}")
    public ResponseEntity<SatelliteConstellation> getConstellation(@PathVariable String constellationName) {
        return spaceOperationCenterService.get(constellationName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Добавить спутник (используя вашу DTO)
    @PostMapping("/add-satellites")
    public ResponseEntity<String> addSatellite(@RequestBody com.satellite.app.service.AddSatelliteRequest request) {
        try {
            spaceOperationCenterService.addSatellite(request);
            return ResponseEntity.ok("Satellite added to constellation " + request.getConstellationName());
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Выполнить миссию
    @PostMapping("/missions")
    public ResponseEntity<String> executeMission(@RequestBody MissionRequest request) {
        try {
            spaceOperationCenterService.executeMission(request);
            if (request.getSatelliteName() != null && !request.getSatelliteName().isBlank()) {
                return ResponseEntity.ok("Mission for satellite " + request.getSatelliteName() + " executed");
            }
            return ResponseEntity.ok("Mission for constellation " + request.getConstellationName() + " executed");
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Активировать спутник
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

    // Деактивировать спутник
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

    // Получить текстовый обзор
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

    // Сделать снимок (для спутников ДЗЗ)
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
                return ResponseEntity.badRequest().body("Satellite " + satelliteName + " is not an imaging satellite");
            }
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Передать данные (для спутников связи)
    @PostMapping("/constellations/{constellationName}/satellites/{satelliteName}/send-data")
    public ResponseEntity<String> sendData(
            @PathVariable String constellationName,
            @PathVariable String satelliteName,
            @RequestParam double dataSize) {

        try {
            var satellite = spaceOperationCenterService.findSatellite(constellationName, satelliteName);
            if (satellite instanceof CommunicationSatellite commSat) {
                spaceOperationCenterService.sendData(constellationName, commSat, dataSize);
                return ResponseEntity.ok("Sent " + dataSize + " GB of data through satellite " + satelliteName);
            } else {
                return ResponseEntity.badRequest().body("Satellite " + satelliteName + " is not a communication satellite");
            }
        } catch (Exception e) {
            return errorResponse(e);
        }
    }

    // Maps an exception to a response: 404 when something is not found, 400 for bad input, 500 otherwise
    private ResponseEntity<String> errorResponse(Exception e) {
        HttpStatus status;
        if (e instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (e instanceof IllegalArgumentException || e instanceof SpaceOperationException) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status).body("Error: " + e.getMessage());
    }
}
