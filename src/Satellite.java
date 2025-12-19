public abstract class Satellite {

    protected String name;
    protected boolean isActive;
    protected double batteryLevel;



public Satellite(String name, double batteryLevel) {
    this.name = name;
    this.batteryLevel = Math.max(0.0, Math.min(1.0, batteryLevel));
    this.isActive = false;
    System.out.println("Создан спутник:" + name + "заряд" + (int) (batteryLevel) * 100 + "%");
}
    // Абстрактный метод для выполнения миссии (будет реализован в наследниках)
    public abstract void performMission();


public String getName() {
    return name;
}

public boolean isActive() {
    return isActive;
}

public double getBatteryLevel() {
    return batteryLevel;
}

// Метод для включения спутника
public boolean activate() {
    if (batteryLevel > 0.2 && !isActive) {
        isActive = true;
        System.out.println(name + ": спутник включен");
        return true;
    } else {
        System.out.println(name + ": невозможно включить - низкий заряд батареи (" + (int)(batteryLevel * 100) + "%)");
        return false;
    }
}

// Метод для выключения спутника
public void deactivate() {
    isActive = false;
    System.out.println(name + ": спутник выключен");
}

public void consumeBattery(double amount) {
    if (amount < 0) {
        System.out.println(name +  ": ошибка - значение расхода заряда не может быть отрицательным");
        return;
    }

    batteryLevel = Math.max(0, batteryLevel - amount);

    if (batteryLevel <= 0) {
        batteryLevel = 0;
        isActive = false;
        System.out.println(name + ": батарея полностью разряжена, спутник выключен");
    } else {
        System.out.println(name + ": расход батареи " + (int)(amount * 100) + "%, остаток: " + (int)(batteryLevel * 100) + "%");
    }

}}