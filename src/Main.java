import java.util.List;
import java.util.Scanner;

public class Main {

    private static Scanner scanner = new Scanner(System.in);
    private static SatelliteConstellation constellation;

    public static void main(String[] args) {
        initializeSystem();
        showMainMenu();
    }

    private static void initializeSystem() {
        System.out.println("STARTING THE SATELLITE CONSTELLATION CONTROL SYSTEM");
        System.out.println("============================================================\n");

        // Create satellites
        System.out.println("CREATING SPECIALIZED SATELLITES:");
        System.out.println("---------------------------------------------");

        Satellite sat1 = new CommunicationSatellite("Comm-1", 0.85, 500.0);
        Satellite sat2 = new CommunicationSatellite("Comm-2", 0.75, 1000.0);
        Satellite sat3 = new ImagingSatellite("Imager-1", 0.92, 2.5);
        Satellite sat4 = new ImagingSatellite("Imager-2", 0.45, 1.0);
        Satellite sat5 = new ImagingSatellite("Imager-3", 0.15, 0.5);

        System.out.println("---------------------------------------------\n");

        // Create the constellation
        System.out.println("Satellite constellation created: RU Basic");
        System.out.println("---------------------------------------------\n");

        constellation = new SatelliteConstellation("RU Basic");

        // Add satellites to the constellation
        System.out.println("BUILDING THE CONSTELLATION:");
        System.out.println("-----------------------------------");

        constellation.addSatellite(sat1);
        constellation.addSatellite(sat2);
        constellation.addSatellite(sat3);
        constellation.addSatellite(sat4);
        constellation.addSatellite(sat5);

        System.out.println("-----------------------------------\n");

        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    private static void showMainMenu() {
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
                    return;
                default:
                    System.out.println("Invalid choice!");
            }

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void showStatusMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                 CONSTELLATION STATUS                  ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        constellation.printConstellationStatus();

        System.out.println("\n1. Show detailed information");
        System.out.println("2. Back");
        System.out.print("Choose: ");

        int choice = getIntInput();
        if (choice == 1) {
            showDetailedStatus();
        }
    }

    private static void showDetailedStatus() {
        clearScreen();
        System.out.println("DETAILED SATELLITE INFORMATION:\n");

        List<Satellite> satellites = constellation.getSatellites();
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

    private static void showActivationMenu() {
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
                constellation.activateAllSatellites();
                break;
            case 2:
                constellation.deactivateAllSatellites();
                break;
            case 3:
                activateByTypeMenu();
                break;
        }
    }

    private static void activateByTypeMenu() {
        System.out.println("\nACTIVATION BY TYPE:");
        System.out.println("1. All communication satellites");
        System.out.println("2. All imaging satellites");
        System.out.print("Choose: ");

        int choice = getIntInput();

        List<Satellite> satellites = constellation.getSatellites();
        int activated = 0;

        for (Satellite sat : satellites) {
            if (choice == 1 && sat instanceof CommunicationSatellite) {
                if (sat.activate()) {
                    activated++;
                }
            } else if (choice == 2 && sat instanceof ImagingSatellite) {
                if (sat.activate()) {
                    activated++;
                }

            }
        }

        System.out.println("Activated: " + activated + " satellites");
    }

    private static void showMissionMenu() {
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
                System.out.println("\nEXECUTING MISSIONS OF CONSTELLATION " +
                        constellation.getConstellationName().toUpperCase());
                System.out.println("==================================================");
                constellation.executeAllMissions();
                break;
            case 2:
                executeCommunicationMissions();
                break;
            case 3:
                executeImagingMissions();
                break;
            case 4:
                testDataTransmission();
                break;
            case 5:
                testImaging();
                break;
        }
    }

    private static void executeCommunicationMissions() {
        List<CommunicationSatellite> commSats =
                constellation.getSatellitesByType(CommunicationSatellite.class);

        System.out.println("\nEXECUTING COMMUNICATION MISSIONS:");
        for (CommunicationSatellite sat : commSats) {
            if (sat.isActive()) {
                sat.performMission();
            }
        }
    }

    private static void executeImagingMissions() {
        List<ImagingSatellite> imagingSats =
                constellation.getSatellitesByType(ImagingSatellite.class);

        System.out.println("\nEXECUTING IMAGING MISSIONS:");
        for (ImagingSatellite sat : imagingSats) {
            if (sat.isActive()) {
                sat.performMission();
            }
        }
    }

    private static void testDataTransmission() {
        System.out.print("Enter the amount of data to transmit (GB): ");
        double dataSize = getDoubleInput();

        List<CommunicationSatellite> commSats =
                constellation.getSatellitesByType(CommunicationSatellite.class);

        for (CommunicationSatellite sat : commSats) {
            if (sat.isActive()) {
                sat.sendData(dataSize);
            }
        }
    }

    private static void testImaging() {
        List<ImagingSatellite> imagingSats =
                constellation.getSatellitesByType(ImagingSatellite.class);

        for (ImagingSatellite sat : imagingSats) {
            if (sat.isActive()) {
                sat.takePhoto();
            }
        }
    }

    private static void showSatelliteControlMenu() {
        clearScreen();
        System.out.println("INDIVIDUAL SATELLITE CONTROL:\n");

        List<Satellite> satellites = constellation.getSatellites();
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

    private static void controlSatellite(Satellite satellite) {
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
                    satellite.deactivate();
                } else {
                    satellite.activate();
                }
                break;
            case 2:
                satellite.performMission();
                break;
            case 3:
                if (satellite instanceof ImagingSatellite imagingSat) {
                    imagingSat.takePhoto();
                } else if (satellite instanceof CommunicationSatellite commSat) {
                    System.out.print("Enter the amount of data (GB): ");
                    double dataSize = getDoubleInput();
                    commSat.sendData(dataSize);
                }
                break;
            case 4:
                showSatelliteDetails(satellite);
                break;
        }
    }

    private static void showSatelliteDetails(Satellite satellite) {
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

    private static void showAddSatelliteMenu() {
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

        Satellite newSatellite = null;

        if (typeChoice == 1) {
            System.out.print("Enter the bandwidth (Mbit/s): ");
            double bandwidth = getDoubleInput();
            newSatellite = new CommunicationSatellite(name, battery, bandwidth);
        } else if (typeChoice == 2) {
            System.out.print("Enter the resolution (m/pixel): ");
            double resolution = getDoubleInput();
            newSatellite = new ImagingSatellite(name, battery, resolution);
        }

        if (newSatellite != null) {
            constellation.addSatellite(newSatellite);
            System.out.println("Satellite added successfully!");
        }
    }

    private static void showSpecialOperationsMenu() {
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

    private static void emergencyShutdown() {
        System.out.println("\n=== EMERGENCY SHUTDOWN ===");
        constellation.deactivateAllSatellites();
        System.out.println("All systems deactivated!");
    }

    private static void systemDiagnostics() {
        System.out.println("\n=== SYSTEM DIAGNOSTICS ===");

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

    private static void emergencyCharge() {
        System.out.println("\n=== EMERGENCY CHARGING ===");

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

    private static void generateReport() {
        System.out.println("\n=== CONSTELLATION REPORT ===");
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
    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Enter a number: ");
            }
        }
    }

    private static double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Enter a number: ");
            }
        }
    }

    private static void clearScreen() {
        // A simple way to "clear" the screen
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
}