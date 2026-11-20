package com.satellite.app.AOP;

import com.satellite.app.Main;
import com.satellite.app.model.ImagingSatelliteParam;
import com.satellite.app.service.AddSatelliteRequest;
import com.satellite.app.service.SpaceOperationCenterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class ExecutionTimeAspectTest {

    @Autowired
    private SpaceOperationCenterService spaceCenter;

    @MockBean
    private Main main; // Mock Main so that the console menu does not run

    @Test
    @DisplayName("ExecutionTimeAspect logs the execution time of methods annotated with @LogExecutionTime")
    void annotatedFacadeMethod_IsInterceptedByAspect(CapturedOutput output) {
        assertTrue(AopUtils.isAopProxy(spaceCenter), "The facade must be wrapped in an AOP proxy");

        spaceCenter.addSatellite(new AddSatelliteRequest("AOP Test",
                new ImagingSatelliteParam("Imager-AOP", 0.9, 1.0)));
        spaceCenter.executeMissions("AOP Test");

        assertTrue(output.getOut().contains("[AOP]") && output.getOut().contains("executeMissions"),
                "The aspect must print the execution time of executeMissions");
    }
}
