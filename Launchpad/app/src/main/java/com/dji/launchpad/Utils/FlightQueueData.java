package com.dji.launchpad.Utils;

import dji.common.flightcontroller.virtualstick.FlightControlData;

public class FlightQueueData {
    private final float pitch;
    private final float roll;
    private final float yaw;
    private final float throttle;
    private final double resetTime;

    public FlightQueueData(float pitch, float roll, float yaw, float throttle, double time) {
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
        this.throttle = throttle;
        resetTime = time;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    public float getYaw() {
        return yaw;
    }

    public float getThrottle() {
        return throttle;
    }

    public double getResetTime() {
        return resetTime;
    }

    public String toString() {
        return "Current Task\n" +
                "Pitch: " + pitch + "\n" +
                "Roll: " + roll + "\n" +
                "Yaw: " + yaw + "\n" +
                "Throttle: " + throttle + "\n" +
                "Time: " + resetTime;
    }
}