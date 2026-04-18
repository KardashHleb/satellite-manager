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
 * Интерактивное меню центра управления. Отключается в Docker и тестах через
 * {@code app.console.menu.enabled=false}, чтобы не блокировать поток запуска.
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
        showMainMenu();
        showRepositoryContents();
    }

    private void initializeSystem() {
        if (!testMode) {
            System.out.println("Нажмите Enter для продолжения...");
            scanner.nextLine();
        }
        System.out.println("ЗАПУСК СИСТЕМЫ УПРАВЛЕНИЯ СПУТНИКОВОЙ ГРУППИРОВКОЙ");
        System.out.println("============================================================\n");
        System.out.println("Группировка \"" + DEFAULT_CONSTELLATION + "\" загружена при старте приложения.");
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

        String status = spaceCenter.getStatus(DEFAULT_CONSTELLATION);
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
        Map<String, SatelliteConstellation> allConstellations = spaceCenter.getAll();

        if (allConstellations.isEmpty()) {
            System.out.println("Репозиторий пуст");
        } else {
            for (Map.Entry<String, SatelliteConstellation> entry : allConstellations.entrySet()) {
                System.out.println("\nГруппировка: " + entry.getKey());
                SatelliteConstellation constellation = entry.getValue();

                System.out.println("Количество спутников: " + constellation.getSatellites().size());

                for (Satellite satellite : constellation.getSatellites()) {
                    System.out.println("  " + satellite.getName() +
                            " - Активен: " + satellite.isActive() +
                            ", Заряд: " + (int) (satellite.getBatteryLevel() * 100) + "%");

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

        List<Satellite> satellites = spaceCenter.getSatellites(DEFAULT_CONSTELLATION);

        for (int i = 0; i < satellites.size(); i++) {
            Satellite sat = satellites.get(i);
            System.out.println((i + 1) + ". " + sat.getName());
            System.out.println("   Тип: " + sat.getClass().getSimpleName());
            System.out.println("   Статус: " + (sat.isActive() ? "АКТИВЕН" : "НЕАКТИВЕН"));
            System.out.println("   Заряд: " + (int) (sat.getBatteryLevel() * 100) + "%");

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
        System.out.println("\nАКТИВАЦИЯ ПО ТИПУ:");
        System.out.println("1. Все спутники связи");
        System.out.println("2. Все спутники ДЗЗ");
        System.out.print("Выберите: ");

        int choice = getIntInput();

        List<Satellite> satellites = spaceCenter.getSatellites(DEFAULT_CONSTELLATION);

        int activated = 0;

        for (Satellite sat : satellites) {
            if (choice == 1 && sat instanceof CommunicationSatellite) {
                spaceCenter.activateSatellite(DEFAULT_CONSTELLATION, sat);
                activated++;
            } else if (choice == 2 && sat instanceof ImagingSatellite) {
                spaceCenter.activateSatellite(DEFAULT_CONSTELLATION, sat);
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
                String name = spaceCenter.getConstellation(DEFAULT_CONSTELLATION)
                        .map(c -> c.getConstellationName().toUpperCase())
                        .orElse("ГРУППИРОВКА");

                System.out.println("\nВЫПОЛНЕНИЕ ВСЕХ МИССИЙ: " + name);
                spaceCenter.executeMissions(DEFAULT_CONSTELLATION);
                break;

            case 2:
                System.out.println("\nЗАПУСК МИССИЙ СВЯЗИ...");
                spaceCenter.executeCommunicationMissions(DEFAULT_CONSTELLATION);
                break;

            case 3:
                System.out.println("\nЗАПУСК МИССИЙ ДЗЗ...");
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
        System.out.print("Введите объем данных для передачи (ГБ): ");
        double dataSize = getDoubleInput();

        List<CommunicationSatellite> commSats =
                spaceCenter.getSatellitesByType(
                        DEFAULT_CONSTELLATION, CommunicationSatellite.class);
        for (CommunicationSatellite sat : commSats) {
            if (sat.isActive()) {
                sat.sendData(dataSize);
            }
        }
    }

    private void testImaging() {
        List<ImagingSatellite> imagingSats = spaceCenter.
                getSatellitesByType(DEFAULT_CONSTELLATION, ImagingSatellite.class);
        for (ImagingSatellite sat : imagingSats) {
            if (sat.isActive()) {
                sat.takePhoto();
            }
        }
    }

    private void showSatelliteControlMenu() {
        clearScreen();
        System.out.println("УПРАВЛЕНИЕ ОТДЕЛЬНЫМИ СПУТНИКАМИ:\n");

        List<Satellite> satellites = spaceCenter.getSatellites(DEFAULT_CONSTELLATION);

        for (int i = 0; i < satellites.size(); i++) {
            Satellite sat = satellites.get(i);
            System.out.printf("%d. %s (%s) - %s, заряд: %d%%%n",
                    i + 1, sat.getName(), sat.getClass().getSimpleName(),
                    sat.isActive() ? "АКТИВЕН" : "НЕАКТИВЕН",
                    (int) (sat.getBatteryLevel() * 100));
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
        System.out.println("Заряд: " + (int) (satellite.getBatteryLevel() * 100) + "%\n");

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
                    System.out.print("Введите объем данных (ГБ): ");
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
        System.out.println("\nДЕТАЛЬНАЯ ИНФОРМАЦИЯ:");
        System.out.println("Имя: " + satellite.getName());
        System.out.println("Тип: " + satellite.getClass().getSimpleName());
        System.out.println("Состояние: " + (satellite.isActive() ? "Активен" : "Неактивен"));
        System.out.println("Уровень заряда: " + (int) (satellite.getBatteryLevel() * 100) + "%");

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

        if (typeChoice == 3) {
            return;
        }

        System.out.print("Введите имя спутника: ");
        String name = scanner.nextLine();

        System.out.print("Введите начальный заряд (0.0-1.0): ");
        double battery = getDoubleInput();

        Satellite newSatellite = null;

        if (typeChoice == 1) {
            System.out.print("Введите пропускную способность (Мбит/с): ");
            double bandwidth = getDoubleInput();
            newSatellite = spaceCenter.createSatellite(
                    new CommunicationSatelliteParam(name, battery, bandwidth)
            );
        } else if (typeChoice == 2) {
            System.out.print("Введите разрешение (м/пиксель): ");
            double resolution = getDoubleInput();
            newSatellite = spaceCenter.createSatellite(
                    new ImagingSatelliteParam(name, battery, resolution)
            );
        }

        if (newSatellite != null) {
            spaceCenter.addSatellite(DEFAULT_CONSTELLATION, newSatellite);
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
                ((ConsoleMenuRunner) AopContext.currentProxy()).generateReport();
                break;
            default:
                break;
        }
    }

    private void emergencyShutdown() {
        System.out.println("\n=== ЭКСТРЕННОЕ ВЫКЛЮЧЕНИЕ ===");

        spaceCenter.deactivateAll(DEFAULT_CONSTELLATION);
        System.out.println("Все системы деактивированы!");
    }

    private void systemDiagnostics() {
        System.out.println("\n=== ДИАГНОСТИКА СИСТЕМЫ ===");

        SatelliteConstellation constellation = spaceCenter.get(DEFAULT_CONSTELLATION).orElseThrow();
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

        SatelliteConstellation constellation = spaceCenter.get(DEFAULT_CONSTELLATION).orElseThrow();
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

    @LogExecutionTime
    public void generateReport() {
        System.out.println("\n=== ОТЧЕТ ПО ГРУППИРОВКЕ ===");

        SatelliteConstellation constellation = spaceCenter.get(DEFAULT_CONSTELLATION).orElseThrow();
        System.out.println("Группировка: " + constellation.getConstellationName());
        System.out.println("Дата: " + java.time.LocalDateTime.now());

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

        System.out.println("\nСТАТИСТИКА:");
        System.out.println("Всего спутников: " + satellites.size());
        System.out.println("Спутников связи: " + commCount);
        System.out.println("Спутников ДЗЗ: " + imagingCount);
        System.out.println("Активных: " + activeCount);
        System.out.println("Всего снимков: " + totalPhotos);
        System.out.println("\nОтчет сформирован успешно!");
    }

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
