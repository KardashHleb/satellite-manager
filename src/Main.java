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
        System.out.println("ЗАПУСК СИСТЕМЫ УПРАВЛЕНИЯ СПУТНИКОВОЙ ГРУППИРОВКОЙ");
        System.out.println("============================================================\n");

        // Создание спутников
        System.out.println("СОЗДАНИЕ СПЕЦИАЛИЗИРОВАННЫХ СПУТНИКОВ:");
        System.out.println("---------------------------------------------");

        Satellite sat1 = new CommunicationSatellite("Связь-1", 0.85, 500.0);
        Satellite sat2 = new CommunicationSatellite("Связь-2", 0.75, 1000.0);
        Satellite sat3 = new ImagingSatellite("ДЗЗ-1", 0.92, 2.5);
        Satellite sat4 = new ImagingSatellite("ДЗЗ-2", 0.45, 1.0);
        Satellite sat5 = new ImagingSatellite("ДЗЗ-3", 0.15, 0.5);

        System.out.println("---------------------------------------------\n");

        // Создание группировки
        System.out.println("Создана спутниковая группировка: RU Basic");
        System.out.println("---------------------------------------------\n");

        constellation = new SatelliteConstellation("RU Basic");

        // Добавление спутников в группировку
        System.out.println("ФОРМИРОВАНИЕ ГРУППИРОВКИ:");
        System.out.println("-----------------------------------");

        constellation.addSatellite(sat1);
        constellation.addSatellite(sat2);
        constellation.addSatellite(sat3);
        constellation.addSatellite(sat4);
        constellation.addSatellite(sat5);

        System.out.println("-----------------------------------\n");

        System.out.println("Нажмите Enter для продолжения...");
        scanner.nextLine();
    }

    private static void showMainMenu() {
        while (true) {
            clearScreen();
            System.out.println("╔═══════════════════════════════════════════════════════╗");
            System.out.println("║      СИСТЕМА УПРАВЛЕНИЯ СПУТНИКОВОЙ ГРУППИРОВКОЙ      ║");
            System.out.println("╠═══════════════════════════════════════════════════════╣");
            System.out.println("║ 1. Показать статус группировки                        ║");
            System.out.println("║ 2. Активировать/деактивировать спутники               ║");
            System.out.println("║ 3. Выполнить миссии                                   ║");
            System.out.println("║ 4. Управление отдельными спутниками                   ║");
            System.out.println("║ 5. Добавить новый спутник                             ║");
            System.out.println("║ 6. Специальные операции                               ║");
            System.out.println("║ 0. Выход                                              ║");
            System.out.println("╚═══════════════════════════════════════════════════════╝");
            System.out.print("\nВыберите действие: ");

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
                    System.out.println("\nЗавершение работы системы...");
                    return;
                default:
                    System.out.println("Неверный выбор!");
            }

            System.out.println("\nНажмите Enter для продолжения...");
            scanner.nextLine();
        }
    }

    private static void showStatusMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                 СТАТУС ГРУППИРОВКИ                    ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        constellation.printConstellationStatus();

        System.out.println("\n1. Показать детальную информацию");
        System.out.println("2. Назад");
        System.out.print("Выберите: ");

        int choice = getIntInput();
        if (choice == 1) {
            showDetailedStatus();
        }
    }

    private static void showDetailedStatus() {
        clearScreen();
        System.out.println("ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О СПУТНИКАХ:\n");

        List<Satellite> satellites = constellation.getSatellites();
        for (int i = 0; i < satellites.size(); i++) {
            Satellite sat = satellites.get(i);
            System.out.println((i + 1) + ". " + sat.getName());
            System.out.println("   Тип: " + sat.getClass().getSimpleName());
            System.out.println("   Статус: " + (sat.isActive() ? "АКТИВЕН" : "НЕАКТИВЕН"));
            System.out.println("   Заряд: " + (int)(sat.getBatteryLevel() * 100) + "%");

            if (sat instanceof ImagingSatellite imagingSat) {
                System.out.println("   Разрешение: " + imagingSat.getResolution() + " м/пиксель");
                System.out.println("   Снимков сделано: " + imagingSat.getPhotosTaken());
            } else if (sat instanceof CommunicationSatellite commSat) {
                System.out.println("   Пропускная способность: " + commSat.getBandwidth() + " Мбит/с");
            }
            System.out.println();
        }
    }

    private static void showActivationMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║           АКТИВАЦИЯ / ДЕАКТИВАЦИЯ СПУТНИКОВ           ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        System.out.println("1. Активировать все спутники");
        System.out.println("2. Деактивировать все спутники");
        System.out.println("3. Активировать по типу");
        System.out.println("4. Назад");
        System.out.print("Выберите: ");

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
        System.out.println("\nАКТИВАЦИЯ ПО ТИПУ:");
        System.out.println("1. Все спутники связи");
        System.out.println("2. Все спутники ДЗЗ");
        System.out.print("Выберите: ");

        int choice = getIntInput();

        List<Satellite> satellites = constellation.getSatellites();
        int activated = 0;

        for (Satellite sat : satellites) {
            if (choice == 1 && sat instanceof CommunicationSatellite) {
                sat.activate();
                activated++;
            } else if (choice == 2 && sat instanceof ImagingSatellite) {
                sat.activate();
                activated++;

            }
        }

        System.out.println("Активировано: " + activated + " спутников");
    }

    private static void showMissionMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                ВЫПОЛНЕНИЕ МИССИЙ                      ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        System.out.println("1. Выполнить все миссии");
        System.out.println("2. Выполнить миссии спутников связи");
        System.out.println("3. Выполнить миссии спутников ДЗЗ");
        System.out.println("4. Тестовая передача данных");
        System.out.println("5. Тестовая съемка");
        System.out.println("6. Назад");
        System.out.print("Выберите: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                System.out.println("\nВЫПОЛНЕНИЕ МИССИЙ ГРУППИРОВКИ " +
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

        System.out.println("\nВЫПОЛНЕНИЕ МИССИЙ СВЯЗИ:");
        for (CommunicationSatellite sat : commSats) {
            if (sat.isActive()) {
                sat.performMission();
            }
        }
    }

    private static void executeImagingMissions() {
        List<ImagingSatellite> imagingSats =
                constellation.getSatellitesByType(ImagingSatellite.class);

        System.out.println("\nВЫПОЛНЕНИЕ МИССИЙ ДЗЗ:");
        for (ImagingSatellite sat : imagingSats) {
            if (sat.isActive()) {
                sat.performMission();
            }
        }
    }

    private static void testDataTransmission() {
        System.out.print("Введите объем данных для передачи (ГБ): ");
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
        System.out.println("УПРАВЛЕНИЕ ОТДЕЛЬНЫМИ СПУТНИКАМИ:\n");

        List<Satellite> satellites = constellation.getSatellites();
        for (int i = 0; i < satellites.size(); i++) {
            Satellite sat = satellites.get(i);
            System.out.printf("%d. %s (%s) - %s, заряд: %d%%\n",
                    i + 1, sat.getName(), sat.getClass().getSimpleName(),
                    sat.isActive() ? "АКТИВЕН" : "НЕАКТИВЕН",
                    (int)(sat.getBatteryLevel() * 100));
        }

        System.out.print("\nВыберите спутник (0 - назад): ");
        int choice = getIntInput();

        if (choice > 0 && choice <= satellites.size()) {
            controlSatellite(satellites.get(choice - 1));
        }
    }

    private static void controlSatellite(Satellite satellite) {
        clearScreen();
        System.out.println("УПРАВЛЕНИЕ СПУТНИКОМ: " + satellite.getName());
        System.out.println("Тип: " + satellite.getClass().getSimpleName());
        System.out.println("Статус: " + (satellite.isActive() ? "АКТИВЕН" : "НЕАКТИВЕН"));
        System.out.println("Заряд: " + (int)(satellite.getBatteryLevel() * 100) + "%\n");

        System.out.println("1. " + (satellite.isActive() ? "Деактивировать" : "Активировать"));
        System.out.println("2. Выполнить миссию");

        if (satellite instanceof ImagingSatellite) {
            System.out.println("3. Сделать снимок");
        } else if (satellite instanceof CommunicationSatellite) {
            System.out.println("3. Передать данные");
        }

        System.out.println("4. Показать детальную информацию");
        System.out.println("0. Назад");
        System.out.print("Выберите: ");

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
                    System.out.print("Введите объем данных (ГБ): ");
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
        System.out.println("\nДЕТАЛЬНАЯ ИНФОРМАЦИЯ:");
        System.out.println("Имя: " + satellite.getName());
        System.out.println("Тип: " + satellite.getClass().getSimpleName());
        System.out.println("Состояние: " + (satellite.isActive() ? "Активен" : "Неактивен"));
        System.out.println("Уровень заряда: " + (int)(satellite.getBatteryLevel() * 100) + "%");

        if (satellite instanceof ImagingSatellite imagingSat) {
            System.out.println("Разрешение: " + imagingSat.getResolution() + " м/пиксель");
            System.out.println("Сделано снимков: " + imagingSat.getPhotosTaken());
        } else if (satellite instanceof CommunicationSatellite commSat) {
            System.out.println("Пропускная способность: " + commSat.getBandwidth() + " Мбит/с");
        }
    }

    private static void showAddSatelliteMenu() {
        clearScreen();
        System.out.println("ДОБАВЛЕНИЕ НОВОГО СПУТНИКА:\n");

        System.out.println("1. Спутник связи");
        System.out.println("2. Спутник ДЗЗ");
        System.out.println("3. Назад");
        System.out.print("Выберите тип: ");

        int typeChoice = getIntInput();

        if (typeChoice == 3) return;

        System.out.print("Введите имя спутника: ");
        String name = scanner.nextLine();

        System.out.print("Введите начальный заряд (0.0-1.0): ");
        double battery = getDoubleInput();

        Satellite newSatellite = null;

        if (typeChoice == 1) {
            System.out.print("Введите пропускную способность (Мбит/с): ");
            double bandwidth = getDoubleInput();
            newSatellite = new CommunicationSatellite(name, battery, bandwidth);
        } else if (typeChoice == 2) {
            System.out.print("Введите разрешение (м/пиксель): ");
            double resolution = getDoubleInput();
            newSatellite = new ImagingSatellite(name, battery, resolution);
        }

        if (newSatellite != null) {
            constellation.addSatellite(newSatellite);
            System.out.println("Спутник успешно добавлен!");
        }
    }

    private static void showSpecialOperationsMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║              СПЕЦИАЛЬНЫЕ ОПЕРАЦИИ                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        System.out.println("1. Экстренное выключение всех систем");
        System.out.println("2. Проверка работоспособности");
        System.out.println("3. Аварийная зарядка спутников");
        System.out.println("4. Сформировать отчет");
        System.out.println("5. Назад");
        System.out.print("Выберите: ");

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
        System.out.println("\n=== ЭКСТРЕННОЕ ВЫКЛЮЧЕНИЕ ===");
        constellation.deactivateAllSatellites();
        System.out.println("Все системы деактивированы!");
    }

    private static void systemDiagnostics() {
        System.out.println("\n=== ДИАГНОСТИКА СИСТЕМЫ ===");

        List<Satellite> satellites = constellation.getSatellites();
        int operational = 0;
        int lowBattery = 0;

        for (Satellite sat : satellites) {
            if (sat.getBatteryLevel() > 0.2) {
                operational++;
            } else {
                lowBattery++;
                System.out.println("ВНИМАНИЕ: " + sat.getName() + " - низкий заряд!");
            }
        }

        System.out.println("\nИТОГИ ДИАГНОСТИКИ:");
        System.out.println("Работоспособных: " + operational);
        System.out.println("С низким зарядом: " + lowBattery);
        System.out.println("Всего спутников: " + satellites.size());
    }

    private static void emergencyCharge() {
        System.out.println("\n=== АВАРИЙНАЯ ЗАРЯДКА ===");

        List<Satellite> satellites = constellation.getSatellites();
        int charged = 0;

        for (Satellite sat : satellites) {
            if (sat.getBatteryLevel() < 0.3) {
                // "Заряжае м" до 50%
                System.out.println(sat.getName() + ": заряд повышен до 50%");
                // В реальной системе здесь был бы метод для установки уровня заряда
                charged++;
            }
        }

        System.out.println("Заряжено спутников: " + charged);
    }

    private static void generateReport() {
        System.out.println("\n=== ОТЧЕТ ПО ГРУППИРОВКЕ ===");
        System.out.println("Группировка: " + constellation.getConstellationName());
        System.out.println("Дата: " + java.time.LocalDateTime.now());

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

        System.out.println("\nСТАТИСТИКА:");
        System.out.println("Всего спутников: " + satellites.size());
        System.out.println("Спутников связи: " + commCount);
        System.out.println("Спутников ДЗЗ: " + imagingCount);
        System.out.println("Активных: " + activeCount);
        System.out.println("Всего снимков: " + totalPhotos);
        System.out.println("\nОтчет сформирован успешно!");
    }

    // Вспомогательные методы
    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Введите число: ");
            }
        }
    }

    private static double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Введите число: ");
            }
        }
    }

    private static void clearScreen() {
        // Простой способ "очистки" экрана
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
}