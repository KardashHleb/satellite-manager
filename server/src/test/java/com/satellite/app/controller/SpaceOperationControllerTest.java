package com.satellite.app.controller;

import com.satellite.app.exception.NotFoundException;
import com.satellite.app.model.CommunicationSatelliteParam;
import com.satellite.app.model.enums.SatelliteType;
import com.satellite.app.service.AddSatelliteRequest;
import com.satellite.app.service.SpaceOperationCenterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Unit tests for SpaceOperationController")
class SpaceOperationControllerTest {

    private SpaceOperationCenterService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(SpaceOperationCenterService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SpaceOperationController(service)).build();
    }

    @Test
    @DisplayName("Unknown constellation returns 404")
    void activate_UnknownConstellation_Returns404() throws Exception {
        when(service.getSatellites("Missing")).thenThrow(new NotFoundException("Constellation not found: Missing"));

        mockMvc.perform(post("/api/constellations/Missing/satellites/Comm-1/activate"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Unknown satellite returns 404")
    void activate_UnknownSatellite_Returns404() throws Exception {
        when(service.getSatellites("RU Basic")).thenReturn(List.of());

        mockMvc.perform(post("/api/constellations/RU Basic/satellites/Nope/activate"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Unknown mission target type returns 400")
    void mission_UnknownTargetType_Returns400() throws Exception {
        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"PLANET\",\"constellationName\":\"RU Basic\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Unexpected error returns 500")
    void mission_UnexpectedError_Returns500() throws Exception {
        doThrow(new IllegalStateException("boom")).when(service).executeMissions("RU Basic");

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"CONSTELLATION\",\"constellationName\":\"RU Basic\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("add-satellites deserializes the satellite type from JSON")
    void addSatellite_JsonWithType_PassesTypedParamToService() throws Exception {
        mockMvc.perform(post("/api/add-satellites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"constellationName\":\"RU Basic\",\"satelliteParam\":"
                                + "{\"type\":\"COMMUNICATION\",\"name\":\"Comm-9\",\"batteryLevel\":0.8,\"bandwidth\":300.0}}"))
                .andExpect(status().isOk());

        ArgumentCaptor<AddSatelliteRequest> captor = ArgumentCaptor.forClass(AddSatelliteRequest.class);
        verify(service).addSatellite(captor.capture());
        assertInstanceOf(CommunicationSatelliteParam.class, captor.getValue().getSatelliteParam());
        assertEquals(SatelliteType.COMMUNICATION, captor.getValue().getSatelliteParam().getType());
    }
}
