package com.dji.launchpad.Utils;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class FlightQueue {

    private final LinkedList<FlightQueueData> mFlightQueue;

    public FlightQueue () {
        mFlightQueue = new LinkedList<>();
    }

    /**
     * constructs and adds flight data object to queue
     * @param pitch float angle +/- 30 degrees
     * @param roll float angle +/- 30 degrees
     * @param yaw float direct angle +/- 180 clockwise from true north
     * @param throttle float max 4m/s
     * @param time double less than 30 seconds
     */
    public void addFlightData (float pitch, float roll, float yaw, float throttle, double time) {
        if (time > 30) {
            time = 30;
        }
        mFlightQueue.addLast(new FlightQueueData(pitch, roll, yaw, throttle, time));
    }

    /**
     * @return returns and removes next set of flight data in queue
     */
    public FlightQueueData getNextFlightData () {
        try {
            return mFlightQueue.removeFirst();
        }
        catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * clears queue of all flight data objects
     */
    public void clearFlightData () {
        mFlightQueue.clear();
    }
}
