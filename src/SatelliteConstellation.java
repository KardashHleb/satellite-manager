import java.util.ArrayList;
import java.util.List;

class SatelliteConstellation {
    private final String  constellationName;
    private List<Satellite> satellites;

    public SatelliteConstellation(String constellationName) {
        this.constellationName = constellationName;
        this.satellites = new ArrayList<>();
        System.out.println("Создана спутниковая группировка: " + constellationName);
    }
    public String getConstellationName() {
        return constellationName;
    }

    // Получение списка спутников
    public List<Satellite> getSatellites() {
        return new ArrayList<>(satellites); // Возвращаем копию для защиты инкапсуляции
    }
    // Добавление спутника в группировки (агрегация)
    public void addSatellite(Satellite satellite) {
        if (satellite != null) {
            satellites.add(satellite);
            System.out.println("Спутник '" + satellite.getName() + "' добавлен в группировку '" + constellationName + "'");
        }
    }
    // Удаление спутника из группировки вдруг понадобится
    public void removeSatellite(Satellite satellite) {
        if (satellites.remove(satellite)) {
            System.out.println("Спутник '" + satellite.getName() + "' удален из группировки '" + constellationName + "'");
        }
    }

    // Выполнение всех миссий (полиморфизм)
    public void executeAllMissions() {
        System.out.println("\n=== Выполнение миссий группировки спутников: '" + constellationName + "' ===");

        int activeMissions = 0;
        for (Satellite satellite : satellites) {
            if (satellite.isActive()) {
                satellite.performMission(); // Полиморфизм - вызовется нужная реализация
                activeMissions++;
            } else {
                System.out.println(satellite.getName() + ": пропущен (не активен)");
            }
        }

        System.out.println("Выполнено миссий: " + activeMissions + " из " + satellites.size());
    }

    // Включение всех спутников в группировке
    public void activateAllSatellites() {
        System.out.println("\n=== Активация всех спутников группировки '" + constellationName + "' ===");
        for (Satellite satellite : satellites) {
            satellite.activate();
        }
    }

    // Выключение всех спутников группировки
    public void deactivateAllSatellites() {
        System.out.println("\n=== Деактивация всех спутников группировки '" + constellationName + "' ===");
        for (Satellite satellite : satellites) {
            satellite.deactivate();
        }
    }

    // Получение статистики по спутникам
    public void printConstellationStatus() {
        System.out.println("\n=== Статус группировки '" + constellationName + "' ===");
        System.out.println("Всего спутников: " + satellites.size());

        int activeCount = 0;
        double totalBattery = 0;

        for (Satellite satellite : satellites) {
            if (satellite.isActive()) activeCount++;
            totalBattery += satellite.getBatteryLevel();

            String type = satellite.getClass().getSimpleName();
            System.out.printf("  - %s (%s): %s, заряд: %d%%\n",
                    satellite.getName(),
                    type,
                    satellite.isActive() ? "активен" : "неактивен",
                    (int)(satellite.getBatteryLevel() * 100));
        }

        double avgBattery = satellites.isEmpty() ? 0 : totalBattery / satellites.size();
        System.out.printf("Активных: %d, Средний заряд: %d%%\n",
                activeCount, (int)(avgBattery * 100));
    }


    // Получение спутников определенного типа (полиморфизм)
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
















