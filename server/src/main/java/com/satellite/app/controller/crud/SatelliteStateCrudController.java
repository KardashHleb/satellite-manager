package com.satellite.app.controller.crud;

import com.satellite.app.SatelliteState;
import com.satellite.app.dto.crud.UpdateSatelliteStateRequest;
import com.satellite.app.service.CrudManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crud/satellite-states")
public class SatelliteStateCrudController {

    private final CrudManagementService crudManagementService;

    public SatelliteStateCrudController(CrudManagementService crudManagementService) {
        this.crudManagementService = crudManagementService;
    }

    @GetMapping("/{id}")
    public SatelliteState get(@PathVariable Long id) {
        return crudManagementService.getSatelliteState(id);
    }

    @PatchMapping("/{id}")
    public SatelliteState patch(@PathVariable Long id, @Valid @RequestBody UpdateSatelliteStateRequest body) {
        return crudManagementService.updateSatelliteState(id, body);
    }
}
