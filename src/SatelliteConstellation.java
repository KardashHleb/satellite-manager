import java.util.ArrayList;
import java.util.List;

class SatelliteConstellation {
    private final String  constellationName;
    private List<Satellite> satellites;

    public SatelliteConstellation(String constellationName) {
        this.constellationName = constellationName;
        this.satellites = new ArrayList<>();
        System.out.println("Satellite constellation created: " + constellationName);
    }
    public String getConstellationName() {
        return constellationName;
    }

    // Get the list of satellites
    public List<Satellite> getSatellites() {
        return new ArrayList<>(satellites); // Return a copy to protect encapsulation
    }
    // Add a satellite to the constellation (aggregation)
    public void addSatellite(Satellite satellite) {
        if (satellite != null) {
            satellites.add(satellite);
            System.out.println("Satellite '" + satellite.getName() + "' added to constellation '" + constellationName + "'");
        }
    }
    // Remove a satellite from the constellation, in case it is needed
    public void removeSatellite(Satellite satellite) {
        if (satellites.remove(satellite)) {
            System.out.println("Satellite '" + satellite.getName() + "' removed from constellation '" + constellationName + "'");
        }
    }

    // Execute all missions (polymorphism)
    public void executeAllMissions() {
        System.out.println("\n=== Executing missions of constellation '" + constellationName + "' ===");

        int activeMissions = 0;
        for (Satellite satellite : satellites) {
            if (satellite.isActive()) {
                satellite.performMission(); // Polymorphism - the right implementation is called
                activeMissions++;
            } else {
                System.out.println(satellite.getName() + ": skipped (inactive)");
            }
        }

        System.out.println("Missions executed: " + activeMissions + " of " + satellites.size());
    }

    // Activate all satellites in the constellation
    public void activateAllSatellites() {
        System.out.println("\n=== Activating all satellites of constellation '" + constellationName + "' ===");
        for (Satellite satellite : satellites) {
            satellite.activate();
        }
    }

    // Deactivate all satellites in the constellation
    public void deactivateAllSatellites() {
        System.out.println("\n=== Deactivating all satellites of constellation '" + constellationName + "' ===");
        for (Satellite satellite : satellites) {
            satellite.deactivate();
        }
    }

    // Satellite statistics
    public void printConstellationStatus() {
        System.out.println("\n=== Status of constellation '" + constellationName + "' ===");
        System.out.println("Total satellites: " + satellites.size());

        int activeCount = 0;
        double totalBattery = 0;

        for (Satellite satellite : satellites) {
            if (satellite.isActive()) activeCount++;
            totalBattery += satellite.getBatteryLevel();

            String type = satellite.getClass().getSimpleName();
            System.out.printf("  - %s (%s): %s, battery: %d%%\n",
                    satellite.getName(),
                    type,
                    satellite.isActive() ? "active" : "inactive",
                    (int)(satellite.getBatteryLevel() * 100));
        }

        double avgBattery = satellites.isEmpty() ? 0 : totalBattery / satellites.size();
        System.out.printf("Active: %d, average battery level: %d%%\n",
                activeCount, (int)(avgBattery * 100));
    }


    // Get satellites of a given type (polymorphism)
    public <T extends Satellite> List<T> getSatellitesByType(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Satellite satellite : satellites) {
            if (type.isInstance(satellite)) {
                result.add(type.cast(satellite));
            }
        }
        return result;
    }
}
















