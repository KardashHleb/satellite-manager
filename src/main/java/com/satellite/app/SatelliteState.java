package com.satellite.app;

public class SatelliteState {
    private boolean isActive;

    public SatelliteState(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}