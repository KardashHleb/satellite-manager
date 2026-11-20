package com.satellite.app.controller.crud;

import com.satellite.app.model.Satellite;
import com.satellite.app.service.AddSatelliteRequest;
import com.satellite.app.service.CrudManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crud/satellites")
public class SatelliteCrudController {

    private final CrudManagementService crudManagementService;

    public SatelliteCrudController(CrudManagementService crudManagementService) {
        this.crudManagementService = crudManagementService;
    }

    @GetMapping
    public List<Satellite> listAll() {
        return crudManagementService.getAllSatellites();
    }

    @GetMapping("/by-constellation/{constellationName}")
    public List<Satellite> listByConstellation(@PathVariable String constellationName) {
        return crudManagementService.listSatellites(constellationName);
    }

    @GetMapping("/{id}")
    public Satellite get(@PathVariable Long id) {
        return crudManagementService.getSatellite(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Satellite create(@Valid @RequestBody AddSatelliteRequest body) {
        return crudManagementService.addSatelliteFromRequest(body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        crudManagementService.deleteSatellite(id);
    }
}
