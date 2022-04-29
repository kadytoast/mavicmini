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
     * @param pitch float velocity +/- 15 m/s
     * @param roll float velocity +/- 15 m/s
     * @param yaw float angle +/- 180 ref home heading
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
     * @return true if queue is empty
     */
    public boolean isQueueEmpty () {
        return mFlightQueue.isEmpty();
    }

    /**
     * clears queue of all flight data objects
     */
    public void clearFlightData () {
        mFlightQueue.clear();
    }
}
