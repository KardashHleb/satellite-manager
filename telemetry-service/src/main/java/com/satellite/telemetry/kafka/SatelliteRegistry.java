package com.satellite.telemetry.kafka;

import com.satellite.events.SatelliteLifecycleEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр спутников, известных телеметрии (обновляется асинхронно из Kafka).
 */
@Component
public class SatelliteRegistry {

    private final Map<String, Set<String>> satellitesByConstellation = new ConcurrentHashMap<>();

    public void registerCreated(SatelliteLifecycleEvent event) {
        satellitesByConstellation
                .computeIfAbsent(event.constellationName(), k -> ConcurrentHashMap.newKeySet())
                .add(event.satelliteName());
    }

    public void registerDeleted(SatelliteLifecycleEvent event) {
        Set<String> names = satellitesByConstellation.get(event.constellationName());
        if (names != null) {
            names.remove(event.satelliteName());
            if (names.isEmpty()) {
                satellitesByConstellation.remove(event.constellationName());
            }
        }
    }

    public Set<String> getSatelliteNames(String constellationName) {
        return Set.copyOf(
                satellitesByConstellation.getOrDefault(constellationName, Set.of())
        );
    }

    public Map<String, Set<String>> snapshot() {
        return satellitesByConstellation.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> Set.copyOf(e.getValue())
                ));
    }
}
