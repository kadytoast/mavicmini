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
     * @param pitch float
     * @param roll float
     * @param yaw float
     * @param throttle float
     * @param time double
     */
    public void addFlightData (float pitch, float roll, float yaw, float throttle, double time) {
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
