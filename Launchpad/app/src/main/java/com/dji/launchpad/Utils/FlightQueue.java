package com.dji.launchpad.Utils;

import android.widget.TextView;

import com.dji.launchpad.MainActivity;
import com.dji.launchpad.R;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class FlightQueue {

    private final LinkedList<FlightQueueData> mFlightQueue;
    private MainActivity ma;

    public FlightQueue (MainActivity maIN) {
        mFlightQueue = new LinkedList<>();
        ma = maIN;
    }

    /**
     * constructs and adds flight data object to queue
     * @param pitch float velocity +/- 15 m/s
     * @param roll float velocity +/- 15 m/s
     * @param yaw float angle +/- 100 deg/s
     * @param throttle float max 4 m/s
     * @param time double less than 10 seconds
     */
    public void addFlightData (float pitch, float roll, float yaw, float throttle, double time) {
        if (time > 10) {
            time = 10;
        }
        else if (time < 0) {
            time = 0;
        }
        mFlightQueue.addLast(new FlightQueueData(pitch, roll, yaw, throttle, time));
    }

    /**
     * @return returns and removes next set of flight data in queue
     */
    public FlightQueueData getNextFlightData () {
        try {
            TextView queue = ma.findViewById(R.id.textview_flightqueue);
            // get task
            FlightQueueData nextTask = mFlightQueue.removeFirst();
            // set textview
            queue.setText(nextTask.toString());
            return nextTask;
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
