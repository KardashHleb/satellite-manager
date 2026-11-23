package com.satellite.app;
import com.satellite.app.service.AddSatelliteRequest;
import com.satellite.app.model.ImagingSatelliteParam;
import com.satellite.app.model.CommunicationSatelliteParam;
import com.satellite.app.service.SpaceOperationCenterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.NoSuchElementException;

@SpringBootApplication(exclude = {
        R2dbcAutoConfiguration.class,
        ValidationAutoConfiguration.class
})
public class Main implements CommandLineRunner {

    static {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception ignored) {

        }
    }

    private Scanner scanner = new Scanner(System.in, "UTF-8");

    @Autowired
    private SpaceOperationCenterService spaceCenter;

    @Value("${test.mode:false}")
    private boolean testMode;

    // Set to false (e.g. in tests) to start the context without the interactive console menu
    @Value("${satellite.console.enabled:true}")
    private boolean consoleEnabled;


    @SuppressWarnings("unused")
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        if (!consoleEnabled) {
            return;
        }

        initializeSystem();
        showMainMenu();
    }

    public Main() {

    }

    private void initializeSystem() {


        System.out.println("STARTING THE SATELLITE CONSTELLATION CONTROL SYSTEM");
        System.out.println("============================================================\n");
        // CREATE SATELLITES THROUGH FACTORIES
        // 1. Create and add satellites through the facade.
        // The first argument is the constellation name ("RU Basic"), the second is the parameter object.

        Satellite comm1 = spaceCenter.addSatellite(
                new AddSatelliteRequest("RU Basic", new CommunicationSatelliteParam("Comm-1", 0.85, 500.0))
        );

        Satellite comm2 = spaceCenter.addSatellite(
                new AddSatelliteRequest("RU Basic", new CommunicationSatelliteParam("Comm-2", 0.75, 1000.0))
        );

        Satellite img1 = spaceCenter.addSatellite(
                new AddSatelliteRequest("RU Basic", new ImagingSatelliteParam("Imager-1", 0.92, 2.5))
        );

        Satellite img2 = spaceCenter.addSatellite(
                new AddSatelliteRequest("RU Basic", new ImagingSatelliteParam("Imager-2", 0.45, 1.0))
        );

        Satellite img3 = spaceCenter.addSatellite(
                new AddSatelliteRequest("RU Basic", new ImagingSatelliteParam("Imager-3", 0.15, 0.5))
        );


        System.out.println("Constellation initialized and saved in the repository!");
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


        String status = spaceCenter.getStatus("RU Basic");
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
            System.out.println("Repository is empty");
        } else {
            for (Map.Entry<String, SatelliteConstellation> entry : allConstellations.entrySet()) {
                System.out.println("\nConstellation: " + entry.getKey());
                SatelliteConstellation constellation = entry.getValue();

                System.out.println("Number of satellites: " + constellation.getSatellites().size());

                for (Satellite satellite : constellation.getSatellites()) {
                    System.out.println("  " + satellite.getName() +
                            " - Active: " + satellite.isActive() +
                            ", Battery: " + (int)(satellite.getBatteryLevel() * 100) + "%");

                    if (satellite instanceof ImagingSatellite imagingSat) {
                        System.out.println("    Type: imaging, photos: " + imagingSat.getPhotosTaken());
                    } else if (satellite instanceof CommunicationSatellite commSat) {
                        System.out.println("    Type: communication, bandwidth: " + commSat.getBandwidth() + " Mbit/s");
                    }
                }
            }
        }
        System.out.println("\n===========================================");
    }

    private void showDetailedStatus() {
        clearScreen();
        System.out.println("DETAILED SATELLITE INFORMATION:\n");


        List<Satellite> satellites = spaceCenter.getSatellites("RU Basic");

        for (int i = 0; i < satellites.size(); i++) {
            Satellite sat = satellites.get(i);
            System.out.println((i + 1) + ". " + sat.getName());
            System.out.println("   Type: " + sat.getClass().getSimpleName());
            System.out.println("   Status: " + (sat.isActive() ? "ACTIVE" : "INACTIVE"));
            System.out.println("   Battery: " + (int)(sat.getBatteryLevel() * 100) + "%");

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

                spaceCenter.activateAll("RU Basic");
                break;
            case 2:

                spaceCenter.deactivateAll("RU Basic");
                break;
            case 3:
                activateByTypeMenu();
                break;
        }
    }

    private void activateByTypeMenu() {
        System.out.println("\nACTIVATION BY TYPE:");
        System.out.println("1. All communication satellites");
        System.out.println("2. All imaging satellites");
        System.out.print("Choose: ");

        int choice = getIntInput();


        List<Satellite> satellites = spaceCenter.getSatellites("RU Basic");

        int activated = 0;

        for (Satellite sat : satellites) {
            if (choice == 1 && sat instanceof CommunicationSatellite) {
                boolean wasActive = sat.isActive();
                spaceCenter.activateSatellite("RU Basic", sat);
                if (!wasActive && sat.isActive()) {
                    activated++;
                }
            } else if (choice == 2 && sat instanceof ImagingSatellite) {
                boolean wasActive = sat.isActive();
                spaceCenter.activateSatellite("RU Basic", sat);
                if (!wasActive && sat.isActive()) {
                    activated++;
                }
            }
        }

        System.out.println("Activated: " + activated + " satellites");
    }

    private void showMissionMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                   MISSION EXECUTION                   ║");
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
                // Use the facade to get the name (for nicer output)
                String name = spaceCenter.getConstellation("RU Basic")
                        .map(c -> c.getConstellationName().toUpperCase())
                        .orElse("CONSTELLATION");

                System.out.println("\nEXECUTING ALL MISSIONS: " + name);
                spaceCenter.executeMissions("RU Basic"); // General mission
                break;

            case 2:
                System.out.println("\nSTARTING COMMUNICATION MISSIONS...");
                spaceCenter.executeCommunicationMissions("RU Basic"); // Specialized mission
                break;

            case 3:
                System.out.println("\nSTARTING IMAGING MISSIONS...");
                spaceCenter.executeImagingMissions("RU Basic"); // Specialized mission
                break;
            case 4:
                testDataTransmission();
                break;
            case 5:
                testImaging();
                break;
        }
    }

    private void executeCommunicationMissions() {

        List<CommunicationSatellite> commSats =
                spaceCenter.getSatellitesByType("RU Basic", CommunicationSatellite.class);
        System.out.println("\nEXECUTING COMMUNICATION MISSIONS:");
        for (CommunicationSatellite sat : commSats) {
            if (sat.isActive()) {
                sat.performMission();
            }
        }
    }

    private void executeImagingMissions() {

        List<ImagingSatellite> imagingSats = spaceCenter.
                getSatellitesByType("RU Basic", ImagingSatellite.class);

        System.out.println("\nEXECUTING IMAGING MISSIONS:");
        for (ImagingSatellite sat : imagingSats) {
            if (sat.isActive()) {
                sat.performMission();
            }
        }
    }

    private void testDataTransmission() {
        System.out.print("Enter the amount of data to transmit (GB): ");
        double dataSize = getDoubleInput();

        List<CommunicationSatellite> commSats =
                spaceCenter.getSatellitesByType(
                        "RU Basic", CommunicationSatellite.class);
        for (CommunicationSatellite sat : commSats) {
            if (sat.isActive()) {
                sat.sendData(dataSize);
            }
        }
    }

    private void testImaging() {
        List<ImagingSatellite> imagingSats = spaceCenter.
                getSatellitesByType("RU Basic", ImagingSatellite.class);
        for (ImagingSatellite sat : imagingSats) {
            if (sat.isActive()) {
                sat.takePhoto();
            }
        }
    }

    private void showSatelliteControlMenu() {
        clearScreen();
        System.out.println("INDIVIDUAL SATELLITE CONTROL:\n");

        List<Satellite> satellites = spaceCenter.getSatellites("RU Basic");

        for (int i = 0; i < satellites.size(); i++) {
            Satellite sat = satellites.get(i);
            System.out.printf("%d. %s (%s) - %s, battery: %d%%\n",
                    i + 1, sat.getName(), sat.getClass().getSimpleName(),
                    sat.isActive() ? "ACTIVE" : "INACTIVE",
                    (int)(sat.getBatteryLevel() * 100));
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
        System.out.println("Battery: " + (int)(satellite.getBatteryLevel() * 100) + "%\n");

        System.out.println("1. " + (satellite.isActive() ? "Deactivate" : "Activate"));
        System.out.println("2. Perform mission");

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
                    spaceCenter.deactivateSatellite("RU Basic", satellite);
                } else {
                    spaceCenter.activateSatellite("RU Basic", satellite);
                }
                break;
            case 2:
                spaceCenter.performSatelliteMission("RU Basic", satellite);
                break;
            case 3:
                if (satellite instanceof ImagingSatellite imagingSat) {
                    spaceCenter.takePhoto("RU Basic", imagingSat);
                } else if (satellite instanceof CommunicationSatellite commSat) {
                    System.out.print("Enter the amount of data (GB): ");
                    double dataSize = getDoubleInput();
                    spaceCenter.sendData("RU Basic", commSat, dataSize);
                }
                break;
            case 4:
                showSatelliteDetails(satellite);
                break;
        }
    }

    private void showSatelliteDetails(Satellite satellite) {
        System.out.println("\nDETAILED INFORMATION:");
        System.out.println("Name: " + satellite.getName());
        System.out.println("Type: " + satellite.getClass().getSimpleName());
        System.out.println("State: " + (satellite.isActive() ? "Active" : "Inactive"));
        System.out.println("Battery level: " + (int)(satellite.getBatteryLevel() * 100) + "%");

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
        System.out.print("Choose a type: ");

        int typeChoice = getIntInput();

        if (typeChoice == 3) return;

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
            // Create through SatelliteService
            newSatellite = spaceCenter.createSatellite(
                    new CommunicationSatelliteParam(name, battery, bandwidth)
            );
        } else if (typeChoice == 2) {
            System.out.print("Enter the resolution (m/pixel): ");
            double resolution = getDoubleInput();
            // Create through SatelliteService
            newSatellite = spaceCenter.createSatellite(
                    new ImagingSatelliteParam(name, battery, resolution)
            );
        }

        if (newSatellite != null) {

            spaceCenter.addSatellite("RU Basic", newSatellite);
            System.out.println("Satellite added successfully!");
        }
    }

    private void showSpecialOperationsMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                  SPECIAL OPERATIONS                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        System.out.println("1. Emergency shutdown of all systems");
        System.out.println("2. Health check");
        System.out.println("3. Emergency charging of satellites");
        System.out.println("4. Generate report");
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
                generateReport();
                break;
        }
    }

    private void emergencyShutdown() {
        System.out.println("\n=== EMERGENCY SHUTDOWN ===");

        spaceCenter.deactivateAll("RU Basic");
        System.out.println("All systems deactivated!");
    }

    private void systemDiagnostics() {
        System.out.println("\n=== SYSTEM DIAGNOSTICS ===");


        SatelliteConstellation constellation = spaceCenter.get("RU Basic").orElseThrow();
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


        SatelliteConstellation constellation = spaceCenter.get("RU Basic").orElseThrow();
        List<Satellite> satellites = constellation.getSatellites();

        int charged = 0;

        for (Satellite sat : satellites) {
            if (sat.getBatteryLevel() < 0.3) {
                System.out.println(sat.getName() + ": battery level raised to 50%");
                charged++;
            }
        }

        System.out.println("Satellites charged: " + charged);
    }
    private void generateReport() {
        System.out.println("\n=== CONSTELLATION REPORT ===");


        SatelliteConstellation constellation = spaceCenter.get("RU Basic").orElseThrow();
        System.out.println("Constellation: " + constellation.getConstellationName());
        System.out.println("Date: " + java.time.LocalDateTime.now());

        List<Satellite> satellites = constellation.getSatellites();
        int commCount = 0;
        int imagingCount = 0;
        int activeCount = 0;
        int totalPhotos = 0;

        for (Satellite sat : satellites) {
            if (sat instanceof CommunicationSatellite commSat) {
                commCount++;
            } else if (sat instanceof ImagingSatellite imagingSat) {
                imagingCount++;
                totalPhotos += imagingSat.getPhotosTaken();
            }

            if (sat.isActive()) activeCount++;
        }

        System.out.println("\nSTATISTICS:");
        System.out.println("Total satellites: " + satellites.size());
        System.out.println("Communication satellites: " + commCount);
        System.out.println("Imaging satellites: " + imagingCount);
        System.out.println("Active: " + activeCount);
        System.out.println("Total photos: " + totalPhotos);
        System.out.println("\nReport generated successfully!");
    }

    // Helper methods
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
                System.out.println("Input error. Exiting the program.");
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
                System.out.println("Input error. Exiting the program.");
                System.exit(0);
            } catch (Exception e) {
                System.out.print("Input error. Enter a number: ");
            }
        }
    }

    private void clearScreen() {
        // A simple way to "clear" the screen
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