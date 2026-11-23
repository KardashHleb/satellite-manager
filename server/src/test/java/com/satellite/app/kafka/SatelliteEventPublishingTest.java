package com.satellite.app.kafka;

import com.satellite.app.model.CommunicationSatelliteParam;
import com.satellite.app.model.Satellite;
import com.satellite.app.service.AddSatelliteRequest;
import com.satellite.app.service.CrudManagementService;
import com.satellite.app.service.SpaceOperationCenterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Satellite lifecycle events carry the saved satellite")
class SatelliteEventPublishingTest {

    private static final String CONSTELLATION = "EventTest-Constellation";

    @Autowired
    private SpaceOperationCenterService spaceCenter;

    @Autowired
    private CrudManagementService crudManagementService;

    @MockitoBean
    private SatelliteEventPublisher satelliteEventPublisher;

    @AfterEach
    void cleanUp() {
        if (spaceCenter.get(CONSTELLATION).isPresent()) {
            crudManagementService.deleteConstellation(CONSTELLATION);
        }
    }

    @Test
    @DisplayName("the CREATED event is published for the saved satellite with its database id")
    void addSatellite_PublishesCreatedWithId() {
        spaceCenter.addSatellite(new AddSatelliteRequest(CONSTELLATION,
                new CommunicationSatelliteParam("Comm-E", 0.8, 100.0)));

        ArgumentCaptor<Satellite> captor = ArgumentCaptor.forClass(Satellite.class);
        verify(satelliteEventPublisher).publishCreated(captor.capture());
        assertNotNull(captor.getValue().getId(), "The event should carry the id of the saved satellite");
        assertEquals("Comm-E", captor.getValue().getName());
    }
}
