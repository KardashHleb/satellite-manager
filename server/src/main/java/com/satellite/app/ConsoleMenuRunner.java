package com.satellite.app;

import com.satellite.app.AOP.LogExecutionTime;
import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.CommunicationSatelliteParam;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.ImagingSatelliteParam;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.service.SpaceOperationCenterService;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Interactive menu of the mission control center. Disabled in Docker and tests with
 * {@code app.console.menu.enabled=false} so that it does not block the startup thread.
 */
@Component
@Order(100)
@ConditionalOnProperty(name = "app.console.menu.enabled", havingValue = "true", matchIfMissing = true)
public class ConsoleMenuRunner implements CommandLineRunner {

    private static final String DEFAULT_CONSTELLATION = "RU Basic";

    private final SpaceOperationCenterService spaceCenter;
    private final boolean testMode;

    private Scanner scanner;

    public ConsoleMenuRunner(SpaceOperationCenterService spaceCenter,
                             @Value("${test.mode:false}") boolean testMode) {
        this.spaceCenter = spaceCenter;
        this.testMode = testMode;
    }

    @Override
    public void run(String... args) {
        this.scanner = new Scanner(System.in, java.nio.charset.StandardCharsets.UTF_8);
        initializeSystem();
        // showMainMenu() prints the repository contents itself when the user chooses "Exit"
        showMainMenu();
    }

    private void initializeSystem() {
        System.out.println("STARTING THE SATELLITE CONSTELLATION CONTROL SYSTEM");
        System.out.println("============================================================\n");
        System.out.println("Constellation \"" + DEFAULT_CONSTELLATION + "\" was loaded at application startup.");
        System.out.println("Constellation initialized and saved to the database!");
        if (!testMode) {
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
        }
    }

    private void showMainMenu() {
        while (true) {
            clearScreen();
            System.out.println("╔═══════════════════════════════════════════════════════╗");
            System.out.println("║        SATELLITE CONSTELLATION CONTROL SYSTEM         ║");
            System.out.println("╠═══════════════════════════════════════════════════════╣");
            System.out.println("║ 1. Show constellation status                          ║");
            System.out.println("║ 2. Activate/deactivate satellites                     ║");
            System.out.println("║ 3. Execute missions                                   ║");
            System.out.println("║ 4. Control individual satellites                      ║");
            System.out.println("║ 5. Add a new satellite                                ║");
            System.out.println("║ 6. Special operations                                 ║");
            System.out.println("║ 0. Exit                                               ║");
            System.out.println("╚═══════════════════════════════════════════════════════╝");
            System.out.print("\nChoose an action: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    showStatusMenu();
                    break;
                case 2:
                    showActivationMenu();
                    break;
                case 3:
                    showMissionMenu();
                    break;
                case 4:
                    showSatelliteControlMenu();
                    break;
                case 5:
                    showAddSatelliteMenu();
                    break;
                case 6:
                    showSpecialOperationsMenu();
                    break;
                case 0:
                    System.out.println("\nShutting down the system...");
                    showRepositoryContents();
                    closeResources();
                    return;
                default:
                    System.out.println("Invalid choice!");
            }

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private void showStatusMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                 CONSTELLATION STATUS                  ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        String status = spaceCenter.getStatus(DEFAULT_CONSTELLATION);
        System.out.println(status);

        System.out.println("\n1. Show detailed information");
        System.out.println("2. Back");
        System.out.print("Choose: ");

        int choice = getIntInput();
        if (choice == 1) {
            showDetailedStatus();
        }
    }

    private void showRepositoryContents() {
        System.out.println("\n\n=== FULL REPOSITORY CONTENTS ===");
        Map<String, SatelliteConstellation> allConstellations = spaceCenter.getAll();

        if (allConstellations.isEmpty()) {
            System.out.println("The repository is empty");
        } else {
            for (Map.Entry<String, SatelliteConstellation> entry : allConstellations.entrySet()) {
                System.out.println("\nConstellation: " + entry.getKey());
                SatelliteConstellation constellation = entry.getValue();

                System.out.println("Number of satellites: " + constellation.getSatellites().size());

                for (Satellite satellite : constellation.getSatellites()) {
                    System.out.println("  " + satellite.getName() +
                            " - Active: " + satellite.isActive() +
                            ", Battery: " + (int) (satellite.getBatteryLevel() * 100) + "%");

                    if (satellite instanceof ImagingSatellite imagingSat) {
                        System.out.println("    Type: imaging, Photos: " + imagingSat.getPhotosTaken());
                    } else if (satellite instanceof CommunicationSatellite commSat) {
                        System.out.println("    Type: communication, Bandwidth: " + commSat.getBandwidth() + " Mbit/s");
                    }
                }
            }
        }
        System.out.println("\n===========================================");
    }

    private void showDetailedStatus() {
        clearScreen();
        System.out.println("DETAILED SATELLITE INFORMATION:\n");

        List<Satellite> satellites = spaceCenter.getSatellites(DEFAULT_CONSTELLATION);

        for (int i = 0; i < satellites.size(); i++) {
            Satellite sat = satellites.get(i);
            System.out.println((i + 1) + ". " + sat.getName());
            System.out.println("   Type: " + sat.getClass().getSimpleName());
            System.out.println("   Status: " + (sat.isActive() ? "ACTIVE" : "INACTIVE"));
            System.out.println("   Battery: " + (int) (sat.getBatteryLevel() * 100) + "%");

            if (sat instanceof ImagingSatellite imagingSat) {
                System.out.println("   Resolution: " + imagingSat.getResolution() + " m/pixel");
                System.out.println("   Photos taken: " + imagingSat.getPhotosTaken());
            } else if (sat instanceof CommunicationSatellite commSat) {
                System.out.println("   Bandwidth: " + commSat.getBandwidth() + " Mbit/s");
            }
            System.out.println();
        }
    }

    private void showActivationMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║          SATELLITE ACTIVATION / DEACTIVATION          ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        System.out.println("1. Activate all satellites");
        System.out.println("2. Deactivate all satellites");
        System.out.println("3. Activate by type");
        System.out.println("4. Back");
        System.out.print("Choose: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                spaceCenter.activateAll(DEFAULT_CONSTELLATION);
                break;
            case 2:
                spaceCenter.deactivateAll(DEFAULT_CONSTELLATION);
                break;
            case 3:
                activateByTypeMenu();
                break;
            default:
                break;
        }
    }

    private void activateByTypeMenu() {
        System.out.println("\nACTIVATION BY TYPE:");
        System.out.println("1. All communication satellites");
        System.out.println("2. All imaging satellites");
        System.out.print("Choose: ");

        int choice = getIntInput();

        List<Satellite> satellites = spaceCenter.getSatellites(DEFAULT_CONSTELLATION);

        int activated = 0;

        for (Satellite sat : satellites) {
            boolean matchesType = (choice == 1 && sat instanceof CommunicationSatellite)
                    || (choice == 2 && sat instanceof ImagingSatellite);
            if (matchesType && spaceCenter.activateSatellite(DEFAULT_CONSTELLATION, sat)) {
                activated++;
            }
        }

        System.out.println("Activated: " + activated + " satellites");
    }

    private void showMissionMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                  MISSION EXECUTION                    ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        System.out.println("1. Execute all missions");
        System.out.println("2. Execute communication satellite missions");
        System.out.println("3. Execute imaging satellite missions");
        System.out.println("4. Test data transmission");
        System.out.println("5. Test imaging");
        System.out.println("6. Back");
        System.out.print("Choose: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                String name = spaceCenter.getConstellation(DEFAULT_CONSTELLATION)
                        .map(c -> c.getConstellationName().toUpperCase())
                        .orElse("CONSTELLATION");

                System.out.println("\nEXECUTING ALL MISSIONS: " + name);
                spaceCenter.executeMissions(DEFAULT_CONSTELLATION);
                break;

            case 2:
                System.out.println("\nSTARTING COMMUNICATION MISSIONS...");
                spaceCenter.executeCommunicationMissions(DEFAULT_CONSTELLATION);
                break;

            case 3:
                System.out.println("\nSTARTING IMAGING MISSIONS...");
                spaceCenter.executeImagingMissions(DEFAULT_CONSTELLATION);
                break;
            case 4:
                testDataTransmission();
                break;
            case 5:
                testImaging();
                break;
            default:
                break;
        }
    }

    private void testDataTransmission() {
        System.out.print("Enter the amount of data to send (GB): ");
        double dataSize = getDoubleInput();

        List<CommunicationSatellite> commSats =
                spaceCenter.getSatellitesByType(
                        DEFAULT_CONSTELLATION, CommunicationSatellite.class);
        for (CommunicationSatellite sat : commSats) {
            if (sat.isActive()) {
                spaceCenter.sendData(DEFAULT_CONSTELLATION, sat, dataSize);
            }
        }
    }

    private void testImaging() {
        List<ImagingSatellite> imagingSats = spaceCenter.
                getSatellitesByType(DEFAULT_CONSTELLATION, ImagingSatellite.class);
        for (ImagingSatellite sat : imagingSats) {
            if (sat.isActive()) {
                spaceCenter.takePhoto(DEFAULT_CONSTELLATION, sat);
            }
        }
    }

    private void showSatelliteControlMenu() {
        clearScreen();
        System.out.println("CONTROL OF INDIVIDUAL SATELLITES:\n");

        List<Satellite> satellites = spaceCenter.getSatellites(DEFAULT_CONSTELLATION);

        for (int i = 0; i < satellites.size(); i++) {
            Satellite sat = satellites.get(i);
            System.out.printf("%d. %s (%s) - %s, battery: %d%%%n",
                    i + 1, sat.getName(), sat.getClass().getSimpleName(),
                    sat.isActive() ? "ACTIVE" : "INACTIVE",
                    (int) (sat.getBatteryLevel() * 100));
        }

        System.out.print("\nChoose a satellite (0 - back): ");
        int choice = getIntInput();

        if (choice > 0 && choice <= satellites.size()) {
            controlSatellite(satellites.get(choice - 1));
        }
    }

    private void controlSatellite(Satellite satellite) {
        clearScreen();
        System.out.println("SATELLITE CONTROL: " + satellite.getName());
        System.out.println("Type: " + satellite.getClass().getSimpleName());
        System.out.println("Status: " + (satellite.isActive() ? "ACTIVE" : "INACTIVE"));
        System.out.println("Battery: " + (int) (satellite.getBatteryLevel() * 100) + "%\n");

        System.out.println("1. " + (satellite.isActive() ? "Deactivate" : "Activate"));
        System.out.println("2. Execute mission");

        if (satellite instanceof ImagingSatellite) {
            System.out.println("3. Take a photo");
        } else if (satellite instanceof CommunicationSatellite) {
            System.out.println("3. Send data");
        }

        System.out.println("4. Show detailed information");
        System.out.println("0. Back");
        System.out.print("Choose: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                if (satellite.isActive()) {
                    spaceCenter.deactivateSatellite(DEFAULT_CONSTELLATION, satellite);
                } else {
                    spaceCenter.activateSatellite(DEFAULT_CONSTELLATION, satellite);
                }
                break;
            case 2:
                spaceCenter.performSatelliteMission(DEFAULT_CONSTELLATION, satellite);
                break;
            case 3:
                if (satellite instanceof ImagingSatellite imagingSat) {
                    spaceCenter.takePhoto(DEFAULT_CONSTELLATION, imagingSat);
                } else if (satellite instanceof CommunicationSatellite commSat) {
                    System.out.print("Enter the amount of data (GB): ");
                    double dataSize = getDoubleInput();
                    spaceCenter.sendData(DEFAULT_CONSTELLATION, commSat, dataSize);
                }
                break;
            case 4:
                showSatelliteDetails(satellite);
                break;
            default:
                break;
        }
    }

    private void showSatelliteDetails(Satellite satellite) {
        System.out.println("\nDETAILED INFORMATION:");
        System.out.println("Name: " + satellite.getName());
        System.out.println("Type: " + satellite.getClass().getSimpleName());
        System.out.println("State: " + (satellite.isActive() ? "Active" : "Inactive"));
        System.out.println("Battery level: " + (int) (satellite.getBatteryLevel() * 100) + "%");

        if (satellite instanceof ImagingSatellite imagingSat) {
            System.out.println("Resolution: " + imagingSat.getResolution() + " m/pixel");
            System.out.println("Photos taken: " + imagingSat.getPhotosTaken());
        } else if (satellite instanceof CommunicationSatellite commSat) {
            System.out.println("Bandwidth: " + commSat.getBandwidth() + " Mbit/s");
        }
    }

    private void showAddSatelliteMenu() {
        clearScreen();
        System.out.println("ADD A NEW SATELLITE:\n");

        System.out.println("1. Communication satellite");
        System.out.println("2. Imaging satellite");
        System.out.println("3. Back");
        System.out.print("Choose the type: ");

        int typeChoice = getIntInput();

        if (typeChoice == 3) {
            return;
        }

        System.out.print("Enter the satellite name: ");
        String name = scanner.nextLine();

        System.out.print("Enter the initial battery level (0.0-1.0): ");
        double battery = getDoubleInput();
        while (battery < 0.0 || battery > 1.0) {
            System.out.print("The battery level must be between 0.0 and 1.0. Enter it again: ");
            battery = getDoubleInput();
        }

        Satellite newSatellite = null;

        if (typeChoice == 1) {
            System.out.print("Enter the bandwidth (Mbit/s): ");
            double bandwidth = getDoubleInput();
            newSatellite = spaceCenter.createSatellite(
                    new CommunicationSatelliteParam(name, battery, bandwidth)
            );
        } else if (typeChoice == 2) {
            System.out.print("Enter the resolution (m/pixel): ");
            double resolution = getDoubleInput();
            newSatellite = spaceCenter.createSatellite(
                    new ImagingSatelliteParam(name, battery, resolution)
            );
        }

        if (newSatellite != null) {
            spaceCenter.addSatellite(DEFAULT_CONSTELLATION, newSatellite);
            System.out.println("Satellite added!");
        }
    }

    private void showSpecialOperationsMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                 SPECIAL OPERATIONS                    ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        System.out.println("1. Emergency shutdown of all systems");
        System.out.println("2. Health check");
        System.out.println("3. Emergency satellite charging");
        System.out.println("4. Generate a report");
        System.out.println("5. Back");
        System.out.print("Choose: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                emergencyShutdown();
                break;
            case 2:
                systemDiagnostics();
                break;
            case 3:
                emergencyCharge();
                break;
            case 4:
                ((ConsoleMenuRunner) AopContext.currentProxy()).generateReport();
                break;
            default:
                break;
        }
    }

    private void emergencyShutdown() {
        System.out.println("\n=== EMERGENCY SHUTDOWN ===");

        spaceCenter.deactivateAll(DEFAULT_CONSTELLATION);
        System.out.println("All systems deactivated!");
    }

    private void systemDiagnostics() {
        System.out.println("\n=== SYSTEM DIAGNOSTICS ===");

        SatelliteConstellation constellation = spaceCenter.get(DEFAULT_CONSTELLATION).orElseThrow();
        List<Satellite> satellites = constellation.getSatellites();

        int operational = 0;
        int lowBattery = 0;

        for (Satellite sat : satellites) {
            if (sat.getBatteryLevel() > 0.2) {
                operational++;
            } else {
                lowBattery++;
                System.out.println("WARNING: " + sat.getName() + " - low battery!");
            }
        }

        System.out.println("\nDIAGNOSTICS SUMMARY:");
        System.out.println("Operational: " + operational);
        System.out.println("Low battery: " + lowBattery);
        System.out.println("Total satellites: " + satellites.size());
    }

    private void emergencyCharge() {
        System.out.println("\n=== EMERGENCY CHARGING ===");

        SatelliteConstellation constellation = spaceCenter.get(DEFAULT_CONSTELLATION).orElseThrow();
        List<Satellite> satellites = constellation.getSatellites();

        int charged = 0;

        for (Satellite sat : satellites) {
            if (sat.getBatteryLevel() < 0.3) {
                System.out.println(sat.getName() + ": battery raised to 50%");
                charged++;
            }
        }

        System.out.println("Satellites charged: " + charged);
    }

    @LogExecutionTime
    public void generateReport() {
        System.out.println("\n=== CONSTELLATION REPORT ===");

        SatelliteConstellation constellation = spaceCenter.get(DEFAULT_CONSTELLATION).orElseThrow();
        System.out.println("Constellation: " + constellation.getConstellationName());
        System.out.println("Date: " + java.time.LocalDateTime.now());

        List<Satellite> satellites = constellation.getSatellites();
        int commCount = 0;
        int imagingCount = 0;
        int activeCount = 0;
        int totalPhotos = 0;

        for (Satellite sat : satellites) {
            if (sat instanceof CommunicationSatellite) {
                commCount++;
            } else if (sat instanceof ImagingSatellite imagingSat) {
                imagingCount++;
                totalPhotos += imagingSat.getPhotosTaken();
            }

            if (sat.isActive()) {
                activeCount++;
            }
        }

        System.out.println("\nSTATISTICS:");
        System.out.println("Total satellites: " + satellites.size());
        System.out.println("Communication satellites: " + commCount);
        System.out.println("Imaging satellites: " + imagingCount);
        System.out.println("Active: " + activeCount);
        System.out.println("Total photos: " + totalPhotos);
        System.out.println("\nReport generated!");
    }

    private int getIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.print("Please enter a number: ");
                    continue;
                }
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Error! Enter an integer: ");
            } catch (NoSuchElementException e) {
                System.out.println("Input error. Exiting.");
                System.exit(0);
            } catch (Exception e) {
                System.out.print("Input error. Enter a number: ");
            }
        }
    }

    private double getDoubleInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.print("Please enter a number: ");
                    continue;
                }
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("Error! Enter a number (integer or decimal with a dot): ");
            } catch (NoSuchElementException e) {
                System.out.println("Input error. Exiting.");
                System.exit(0);
            } catch (Exception e) {
                System.out.print("Input error. Enter a number: ");
            }
        }
    }

    private void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    private void closeResources() {
        try {
            if (scanner != null) {
                scanner.close();
                System.out.println("Resources released.");
            }
        } catch (Exception e) {
            System.err.println("Error while closing resources: " + e.getMessage());
        }
    }
}
