package com.dji.launchpad.Utils;

import dji.common.flightcontroller.virtualstick.FlightControlData;

public class FlightQueueData {
    private final float pitch;
    private final float roll;
    private final float throttle;
    private final double resetTime;

    public FlightQueueData(float pitch, float roll, float throttle, double time) {
        this.pitch = pitch;
        this.roll = roll;
        this.throttle = throttle;
        resetTime = time;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    public float getThrottle() {
        return throttle;
    }

    public double getResetTime() {
        return resetTime;
    }
}