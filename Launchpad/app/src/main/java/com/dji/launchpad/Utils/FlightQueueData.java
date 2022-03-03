package com.dji.launchpad.Utils;

import dji.common.flightcontroller.virtualstick.FlightControlData;

public class FlightQueueData {
    private final FlightControlData flightData;
    private final double resetTime;

    public FlightQueueData(float pitch, float roll, float yaw, float throttle, double time) {
        flightData = new FlightControlData(pitch, roll, yaw, throttle);
        resetTime = time;
    }

    public FlightControlData getFlightData() {
        return flightData;
    }

    public double getResetTime() {
        return resetTime;
    }
}