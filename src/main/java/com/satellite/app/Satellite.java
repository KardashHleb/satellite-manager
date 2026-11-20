package com.satellite.app;

public abstract class Satellite {
    protected String name;
    protected SatelliteState state;
    protected EnergySystem energy;

    public Satellite(String name, double batteryLevel) {
        this.name = name;
        this.energy = new EnergySystem(batteryLevel);
        this.state = new SatelliteState(false);
        System.out.println("Satellite created: " + name + ", battery: " + energy.getBatteryPercentage() + "%");
    }

    // Abstract method that performs the mission (implemented in subclasses)
    public abstract void performMission();

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return state.isActive();
    }

    public double getBatteryLevel() {
        return energy.getBatteryLevel();
    }

    // Activates the satellite
    public boolean activate() {
        if (state.isActive()) {
            System.out.println(name + ": satellite is already active");
            return false;
        }
        if (energy.getBatteryLevel() > 0.2) {
            state.activate();
            System.out.println(name + ": satellite activated");
            return true;
        } else {
            System.out.println(name + ": cannot activate - low battery level (" +
                    energy.getBatteryPercentage() + "%)");
            return false;
        }
    }

    // Deactivates the satellite
    public void deactivate() {
        state.deactivate();
        System.out.println(name + ": satellite deactivated");
    }

}