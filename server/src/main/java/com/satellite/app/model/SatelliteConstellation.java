package com.satellite.app.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "satellite_constellation")
public class SatelliteConstellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "constellation_name", nullable = false, unique = true)
    private String constellationName;

    @OneToMany(mappedBy = "constellation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("constellation-satellites")
    private List<Satellite> satellites = new ArrayList<>();

    public SatelliteConstellation(String constellationName) {
        this.constellationName = constellationName;
        this.satellites = new ArrayList<>();
        System.out.println("Создана спутниковая группировка: " + constellationName);
    }

    public List<Satellite> getSatellites() {
        return new ArrayList<>(satellites);
    }

    public void addSatellite(Satellite satellite) {
        if (satellite != null) {
            satellite.setConstellation(this);
            satellites.add(satellite);
            System.out.println("Спутник '" + satellite.getName() + "' добавлен в группировку '" + constellationName + "'");
        }
    }

    public void removeSatellite(Satellite satellite) {
        if (satellite != null && satellites.remove(satellite)) {
            System.out.println("Спутник '" + satellite.getName() + "' удален из группировки '" + constellationName + "'");
        }
    }

    public void executeAllMissions() {
        System.out.println("\n=== Выполнение миссий группировки спутников: '" + constellationName + "' ===");

        int activeMissions = 0;
        for (Satellite satellite : satellites) {
            if (satellite.isActive()) {
                satellite.performMission();
                activeMissions++;
            } else {
                System.out.println(satellite.getName() + ": пропущен (не активен)");
            }
        }

        System.out.println("Выполнено миссий: " + activeMissions + " из " + satellites.size());
    }

    public void activateAllSatellites() {
        System.out.println("\n=== Активация всех спутников группировки '" + constellationName + "' ===");
        for (Satellite satellite : satellites) {
            satellite.activate();
        }
    }

    public void deactivateAllSatellites() {
        System.out.println("\n=== Деактивация всех спутников группировки '" + constellationName + "' ===");
        for (Satellite satellite : satellites) {
            satellite.deactivate();
        }
    }

    public String printConstellationStatus() {
        StringBuilder status = new StringBuilder();
        status.append("\n=== Статус группировки '").append(constellationName).append("' ===\n");
        status.append("Всего спутников: ").append(satellites.size()).append("\n");

        int activeCount = 0;
        double totalBattery = 0;

        for (Satellite satellite : satellites) {
            if (satellite.isActive()) {
                activeCount++;
            }
            totalBattery += satellite.getBatteryLevel();

            String type = satellite.getClass().getSimpleName();
            status.append(String.format("  - %s (%s): %s, заряд: %d%%\n",
                    satellite.getName(),
                    type,
                    satellite.isActive() ? "активен" : "неактивен",
                    (int) (satellite.getBatteryLevel() * 100)));
        }

        double avgBattery = satellites.isEmpty() ? 0 : totalBattery / satellites.size();
        status.append(String.format("Активных: %d, Средний заряд: %d%%\n",
                activeCount, (int) (avgBattery * 100)));

        return status.toString();
    }

    public <T extends Satellite> List<T> getSatellitesByType(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Satellite satellite : satellites) {
            if (type.isInstance(satellite)) {
                result.add(type.cast(satellite));
            }
        }
        return result;
    }

    public int getActiveSatelliteCount() {
        return (int) satellites.stream()
                .filter(Satellite::isActive)
                .count();
    }

    public double getAverageBattery() {
        if (satellites.isEmpty()) {
            return 0;
        }
        return satellites.stream()
                .mapToDouble(Satellite::getBatteryLevel)
                .average()
                .orElse(0);
    }

    public String getStatusReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Статус группировки '").append(constellationName).append("' ===\n");
        report.append("Всего спутников: ").append(satellites.size()).append("\n");
        report.append("Активных: ").append(getActiveSatelliteCount()).append("\n");
        report.append("Средний заряд: ").append(String.format("%.0f", getAverageBattery() * 100)).append("%\n");
        return report.toString();
    }
}
