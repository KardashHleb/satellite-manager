public class CommunicationSatellite extends Satellite {
    private final double bandwidth; // пропускная способность в Мбит/с
    private double dataSent;  // всего отправлено данных в ГБ

    public CommunicationSatellite(String name, double batteryLevel, double bandwidth) {
        super(name, batteryLevel);
        this.bandwidth = bandwidth;
        this.dataSent = 0;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public double getDataSent() {
        return dataSent;
    }

    @Override
    public void performMission() {
        if (isActive()) {
            System.out.println(name + ": выполняет миссию связи");
            // Автоматически отправляем небольшой объем данных при выполнении миссии
            sendData(0.1); // отправляем 100 МБ
        } else {
            System.out.println(name + ": невозможно выполнить миссию - спутник выключен");
        }
    }

    public void sendData(double dataSizeGB) {
        if (isActive() && getBatteryLevel() > 0.02) {
            // Расход батареи зависит от объема данных
            double batteryConsumption = dataSizeGB * 0.1; // 10% заряда на 1 ГБ
            batteryConsumption = Math.min(batteryConsumption, 0.5); // не более 50% за раз

            energy.consumeBattery(batteryConsumption);
            dataSent += dataSizeGB;

            System.out.println(name + ": отправлено " + dataSizeGB + " ГБ данных. " +
                    "Скорость: " + bandwidth + " Мбит/с. Всего отправлено: " +
                    String.format("%.2f", dataSent) + " ГБ");

            // Проверка разряда батареи после использования
            if (getBatteryLevel() <= 0) {
                state.deactivate();
                System.out.println(name + ": батарея полностью разряжена, спутник выключен");
            }
        } else if (!isActive()) {
            System.out.println(name + ": невозможно отправить данные - спутник выключен");
        } else {
            System.out.println(name + ": невозможно отправить данные - низкий заряд батареи");
        }
    }

    // Дополнительный метод для проверки связи
    public boolean establishConnection() {
        if (isActive() && getBatteryLevel() > 0.1) {
            System.out.println(name + ": соединение установлено, готов к передаче данных");
            return true;
        }
        System.out.println(name + ": не удалось установить соединение");
        return false;
    }
}