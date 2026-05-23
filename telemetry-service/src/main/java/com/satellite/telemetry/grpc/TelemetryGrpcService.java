package com.satellite.telemetry.grpc;

import com.satellite.telemetry.kafka.SatelliteRegistry;
import com.satellite.telemetry.proto.TelemetryRequest;
import com.satellite.telemetry.proto.TelemetryServiceGrpc;
import com.satellite.telemetry.proto.TelemetryUpdate;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@GrpcService
public class TelemetryGrpcService extends TelemetryServiceGrpc.TelemetryServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(TelemetryGrpcService.class);

    private final SatelliteRegistry satelliteRegistry;
    private final Random random = new Random();
    private final Map<StreamObserver<TelemetryUpdate>, ScheduledExecutorService> streamSchedulers = new ConcurrentHashMap<>();

    public TelemetryGrpcService(SatelliteRegistry satelliteRegistry) {
        this.satelliteRegistry = satelliteRegistry;
    }

    @Override
    public void streamTelemetry(TelemetryRequest request, StreamObserver<TelemetryUpdate> responseObserver) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        streamSchedulers.put(responseObserver, scheduler);

        String constellationName = request.getConstellationName();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Set<String> satelliteNames = satelliteRegistry.getSatelliteNames(constellationName);
                if (satelliteNames.isEmpty()) {
                    log.debug("No known satellites for constellation '{}' yet (waiting for Kafka events)", constellationName);
                    return;
                }
                for (String satelliteName : satelliteNames) {
                    TelemetryUpdate update = TelemetryUpdate.newBuilder()
                            .setSatelliteName(satelliteName)
                            .setTemperatureInside(randomTemperature(18.0, 35.0))
                            .setTemperatureOutside(randomTemperature(-80.0, 50.0))
                            .setTimestampUnix(Instant.now().getEpochSecond())
                            .build();
                    responseObserver.onNext(update);
                }
            } catch (Exception ex) {
                responseObserver.onError(ex);
                stopScheduler(responseObserver);
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private double randomTemperature(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    private void stopScheduler(StreamObserver<TelemetryUpdate> responseObserver) {
        ScheduledExecutorService scheduler = streamSchedulers.remove(responseObserver);
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
