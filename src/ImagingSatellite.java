public class ImagingSatellite extends Satellite {
    private final double resolution;
    private int photosTaken;

    public ImagingSatellite(String name, double batteryLevel, double resolution) {
        super(name, batteryLevel);
        this.resolution = resolution;
        this.photosTaken = 0;
    }

    public double getResolution() {
        return resolution;
    }

    public int getPhotosTaken() {
        return photosTaken;
    }

    @Override
    public void performMission() {
        if (isActive) {
            System.out.println(name + ": выполняет съемку территории");
            takePhoto();
        } else {
            System.out.println(name + ": невозможно выполнить миссию - спутник выключен");
        }
    }

    public void takePhoto() {
        if (isActive && batteryLevel > 0.05) {
            consumeBattery(0.05);
            photosTaken++;
            System.out.println(name + ": фото сделано. Разрешение: " + resolution + " м/пиксель. Всего фото: " + photosTaken);
        } else if (!isActive) {
            System.out.println(name + ": невозможно сделать фото - спутник выключен");
        } else {
            System.out.println(name + ": невозможно сделать фото - низкий заряд батареи");
        }
    }

    @Override
    public String toString() {
        return "ImagingSatellite{" +
                "resolution=" + resolution +
                ", photosTaken=" + photosTaken +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                ", batteryLevel=" + batteryLevel +
                '}';
    }
}