package com.satellite.app;


import com.satellite.app.service.SpaceOperationCenterService;
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
    private SpaceOperationCenterService operationCenter;


    @SuppressWarnings("unused")
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {


        initializeSystem();
        showMainMenu();
        showRepositoryContents();
    }

    public Main() {

    }

    private void initializeSystem() {
        System.out.println("ЗАПУСК СИСТЕМЫ УПРАВЛЕНИЯ СПУТНИКОВОЙ ГРУППИРОВКОЙ");
        System.out.println("============================================================\n");

        // 1. Создание спутников (доменные классы) - РАЗРЕШЕНО по заданию
        CommunicationSatellite comm1 = new CommunicationSatellite("Связь-1", 0.85, 500.0);
        CommunicationSatellite comm2 = new CommunicationSatellite("Связь-2", 0.75, 1000.0);
        ImagingSatellite img1 = new ImagingSatellite("ДЗЗ-1", 0.92, 2.5);
        ImagingSatellite img2 = new ImagingSatellite("ДЗЗ-2", 0.45, 1.0);
        ImagingSatellite img3 = new ImagingSatellite("ДЗЗ-3", 0.15, 0.5);

        // 2. Создание группировки через сервис (записывается в базу данных)
        operationCenter.create("RU Basic");

        // 3. Добавление спутников в группировку через сервис (записывается в базу данных)
        operationCenter.addSatellite("RU Basic", comm1);
        operationCenter.addSatellite("RU Basic", comm2);
        operationCenter.addSatellite("RU Basic", img1);
        operationCenter.addSatellite("RU Basic", img2);
        operationCenter.addSatellite("RU Basic", img3);

        System.out.println("Группировка успешно инициализирована и сохранена в базе данных!");
        System.out.println("Нажмите Enter для продолжения...");
        scanner.nextLine();
    }


    private void showMainMenu() {
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
                    showRepositoryContents();
                    closeResources();
                    return;
                default:
                    System.out.println("Неверный выбор!");
            }

            System.out.println("\nНажмите Enter для продолжения...");
            scanner.nextLine();
        }
    }

    private void showStatusMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                 СТАТУС ГРУППИРОВКИ                    ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");


        String status = operationCenter.getStatus("RU Basic");
        System.out.println(status);

        System.out.println("\n1. Показать детальную информацию");
        System.out.println("2. Назад");
        System.out.print("Выберите: ");

        int choice = getIntInput();
        if (choice == 1) {
            showDetailedStatus();
        }
    }


    private void showRepositoryContents() {
        System.out.println("\n\n=== ВЫВОД ВСЕГО СОДЕРЖИМОГО РЕПОЗИТОРИЯ ===");
        Map<String, SatelliteConstellation> allConstellations = operationCenter.getAll();

        if (allConstellations.isEmpty()) {
            System.out.println("Репохизиторий пуст");
        } else {
            for (Map.Entry<String, SatelliteConstellation> entry : allConstellations.entrySet()) {
                System.out.println("\nГруппировка: " + entry.getKey());
                SatelliteConstellation constellation = entry.getValue();

                System.out.println("Количество спутников: " + constellation.getSatellites().size());

                for (Satellite satellite : constellation.getSatellites()) {
                    System.out.println("  " + satellite.getName() +
                            " - Активен: " + satellite.isActive() +
                            ", Заряд: " + (int)(satellite.getBatteryLevel() * 100) + "%");

                    if (satellite instanceof ImagingSatellite imagingSat) {
                        System.out.println("    Тип: ДЗЗ, Снимков: " + imagingSat.getPhotosTaken());
                    } else if (satellite instanceof CommunicationSatellite commSat) {
                        System.out.println("    Тип: Связь, Пропускная способность: " + commSat.getBandwidth() + " Мбит/с");
                    }
                }
            }
        }
        System.out.println("\n===========================================");
    }

    private void showDetailedStatus() {
        clearScreen();
        System.out.println("ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О СПУТНИКАХ:\n");


        List<Satellite> satellites = operationCenter.getSatellites("RU Basic");

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

    private void showActivationMenu() {
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

                operationCenter.activateAll("RU Basic");
                break;
            case 2:

                operationCenter.deactivateAll("RU Basic");
                break;
            case 3:
                activateByTypeMenu();
                break;
        }
    }

    private void activateByTypeMenu() {
        System.out.println("\nАКТИВАЦИЯ ПО ТИПУ:");
        System.out.println("1. Все спутники связи");
        System.out.println("2. Все спутники ДЗЗ");
        System.out.print("Выберите: ");

        int choice = getIntInput();


        List<Satellite> satellites = operationCenter.getSatellites("RU Basic");

        int activated = 0;

        for (Satellite sat : satellites) {
            if (choice == 1 && sat instanceof CommunicationSatellite) {
                operationCenter.activateSatellite("RU Basic", sat);
                activated++;
            } else if (choice == 2 && sat instanceof ImagingSatellite) {
                operationCenter.activateSatellite("RU Basic", sat);
                activated++;
            }
        }

        System.out.println("Активировано: " + activated + " спутников");
    }

    private void showMissionMenu() {
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

                SatelliteConstellation constellation = operationCenter.get("RU Basic").orElseThrow();
                System.out.println("\nВЫПОЛНЕНИЕ МИССИЙ ГРУППИРОВКИ " +
                        constellation.getConstellationName().toUpperCase());
                System.out.println("==================================================");
                operationCenter.executeMissions("RU Basic");
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

    private void executeCommunicationMissions() {

        List<CommunicationSatellite> commSats =
                operationCenter.getSatellitesByType("RU Basic", CommunicationSatellite.class);
        System.out.println("\nВЫПОЛНЕНИЕ МИССИЙ СВЯЗИ:");
        for (CommunicationSatellite sat : commSats) {
            if (sat.isActive()) {
                sat.performMission();
            }
        }
    }

    private void executeImagingMissions() {

        List<ImagingSatellite> imagingSats = operationCenter.
                getSatellitesByType("RU Basic", ImagingSatellite.class);

        System.out.println("\nВЫПОЛНЕНИЕ МИССИЙ ДЗЗ:");
        for (ImagingSatellite sat : imagingSats) {
            if (sat.isActive()) {
                sat.performMission();
            }
        }
    }

    private void testDataTransmission() {
        System.out.print("Введите объем данных для передачи (ГБ): ");
        double dataSize = getDoubleInput();

        List<CommunicationSatellite> commSats =
                operationCenter.getSatellitesByType(
                        "RU Basic", CommunicationSatellite.class);
        for (CommunicationSatellite sat : commSats) {
            if (sat.isActive()) {
                sat.sendData(dataSize);
            }
        }
    }

    private void testImaging() {
        List<ImagingSatellite> imagingSats = operationCenter.
                getSatellitesByType("RU Basic", ImagingSatellite.class);
        for (ImagingSatellite sat : imagingSats) {
            if (sat.isActive()) {
                sat.takePhoto();
            }
        }
    }

    private void showSatelliteControlMenu() {
        clearScreen();
        System.out.println("УПРАВЛЕНИЕ ОТДЕЛЬНЫМИ СПУТНИКАМИ:\n");

        List<Satellite> satellites = operationCenter.getSatellites("RU Basic");

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

    private void controlSatellite(Satellite satellite) {
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
                    operationCenter.deactivateSatellite("RU Basic", satellite);
                } else {
                    operationCenter.activateSatellite("RU Basic", satellite);
                }
                break;
            case 2:
                operationCenter.performSatelliteMission("RU Basic", satellite);
                break;
            case 3:
                if (satellite instanceof ImagingSatellite imagingSat) {
                    operationCenter.takePhoto("RU Basic", imagingSat);
                } else if (satellite instanceof CommunicationSatellite commSat) {
                    System.out.print("Введите объем данных (ГБ): ");
                    double dataSize = getDoubleInput();
                    operationCenter.sendData("RU Basic", commSat, dataSize);
                }
                break;
            case 4:
                showSatelliteDetails(satellite);
                break;
        }
    }

    private void showSatelliteDetails(Satellite satellite) {
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

    private void showAddSatelliteMenu() {
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

            operationCenter.addSatellite("RU Basic", newSatellite);
            System.out.println("Спутник успешно добавлен!");
        }
    }

    private void showSpecialOperationsMenu() {
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

    private void emergencyShutdown() {
        System.out.println("\n=== ЭКСТРЕННОЕ ВЫКЛЮЧЕНИЕ ===");

        operationCenter.deactivateAll("RU Basic");
        System.out.println("Все системы деактивированы!");
    }

    private void systemDiagnostics() {
        System.out.println("\n=== ДИАГНОСТИКА СИСТЕМЫ ===");


        SatelliteConstellation constellation = operationCenter.get("RU Basic").orElseThrow();
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

    private void emergencyCharge() {
        System.out.println("\n=== АВАРИЙНАЯ ЗАРЯДКА ===");


        SatelliteConstellation constellation = operationCenter.get("RU Basic").orElseThrow();
        List<Satellite> satellites = constellation.getSatellites();

        int charged = 0;

        for (Satellite sat : satellites) {
            if (sat.getBatteryLevel() < 0.3) {
                System.out.println(sat.getName() + ": заряд повышен до 50%");
                charged++;
            }
        }

        System.out.println("Заряжено спутников: " + charged);
    }

    private void generateReport() {
        System.out.println("\n=== ОТЧЕТ ПО ГРУППИРОВКЕ ===");


        SatelliteConstellation constellation = operationCenter.get("RU Basic").orElseThrow();
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
    private int getIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.print("Пожалуйста, введите число: ");
                    continue;
                }
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Ошибка! Введите целое число: ");
            } catch (NoSuchElementException e) {
                System.out.println("Ошибка ввода. Завершение программы.");
                System.exit(0);
            } catch (Exception e) {
                System.out.print("Ошибка ввода. Введите число: ");
            }
        }
    }

    private double getDoubleInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.print("Пожалуйста, введите число: ");
                    continue;
                }
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("Ошибка! Введите число (целое или с точкой): ");
            } catch (NoSuchElementException e) {
                System.out.println("Ошибка ввода. Завершение программы.");
                System.exit(0);
            } catch (Exception e) {
                System.out.print("Ошибка ввода. Введите число: ");
            }
        }
    }

    private void clearScreen() {
        // Простой способ "очистки" экрана
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    private void closeResources() {
        try {
            if (scanner != null) {
                scanner.close();
                System.out.println("Ресурсы освобождены.");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при закрытии ресурсов: " + e.getMessage());
        }
    }
}