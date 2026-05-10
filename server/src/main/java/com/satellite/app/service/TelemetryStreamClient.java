package com.satellite.app.service;

import com.satellite.app.model.Satellite;
import com.satellite.app.repository.SatelliteRepository;
import com.satellite.telemetry.proto.TelemetryRequest;
import com.satellite.telemetry.proto.TelemetryServiceGrpc;
import com.satellite.telemetry.proto.TelemetryUpdate;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class TelemetryStreamClient {

    private static final Logger log = LoggerFactory.getLogger(TelemetryStreamClient.class);

    private final SatelliteRepository satelliteRepository;
    private final ExecutorService streamExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean running;

    @GrpcClient("telemetry-service")
    private TelemetryServiceGrpc.TelemetryServiceBlockingStub telemetryStub;

    public TelemetryStreamClient(SatelliteRepository satelliteRepository) {
        this.satelliteRepository = satelliteRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startStreaming() {
        if (running) {
            return;
        }
        running = true;
        streamExecutor.submit(this::streamLoop);
        log.info("Telemetry gRPC client started");
    }

    private void streamLoop() {
        while (running) {
            try {
                TelemetryRequest request = TelemetryRequest.newBuilder()
                        .setConstellationName("RU Basic")
                        .build();
                Iterator<TelemetryUpdate> updates = telemetryStub.streamTelemetry(request);
                while (running && updates.hasNext()) {
                    applyUpdate(updates.next());
                }
            } catch (StatusRuntimeException ex) {
                log.warn("Telemetry stream disconnected: {}", ex.getStatus().getDescription());
                sleepBeforeRetry();
            } catch (Exception ex) {
                log.error("Unexpected telemetry stream error", ex);
                sleepBeforeRetry();
            }
        }
    }

    @Transactional
    public void applyUpdate(TelemetryUpdate update) {
        satelliteRepository.findByName(update.getSatelliteName()).ifPresent(satellite -> {
            satellite.setTemperatureInside(update.getTemperatureInside());
            satellite.setTemperatureOutside(update.getTemperatureOutside());
            satelliteRepository.save(satellite);
        });
    }

    private void sleepBeforeRetry() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        streamExecutor.shutdownNow();
    }
}
