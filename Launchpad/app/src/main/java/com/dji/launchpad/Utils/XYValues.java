package com.dji.launchpad.Utils;

/**
 * simple class to hold two x y offset values (used for positioning offsets)
 */
public class XYValues {
    public final double X;
    public final double Y;

    public XYValues (double xIN, double yIN) {
        X = xIN;
        Y = yIN;
    }

    @Override
    public String toString() {
        return "XYValues{" +
                "X = " + X +
                ", Y = " + Y +
                '}';
    }
}
