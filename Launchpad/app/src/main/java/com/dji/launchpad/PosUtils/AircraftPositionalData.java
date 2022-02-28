package com.dji.launchpad.PosUtils;

import com.google.android.gms.maps.model.LatLng;

import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.model.LocationCoordinate2D;

public class AircraftPositionalData {

    private final LocationCoordinate3D aircraftCurrentLocation;
    private final Attitude aircraftCurrentAttitude;
    private final LocationCoordinate2D aircraftHomeLocation;
    private final double aircraftHomeHeading;
    public final LatLng aircraftLatLng;
    public final LatLng homeLatLng;

    public AircraftPositionalData (LocationCoordinate3D aircraftCurrentLocationIN,
                                   Attitude aircraftCurrentAttitudeIN,
                                   LocationCoordinate2D aircraftHomeLocationIN,
                                   double homeHeadingIN) {
        // define class vars
        aircraftCurrentLocation = aircraftCurrentLocationIN;
        aircraftCurrentAttitude = aircraftCurrentAttitudeIN;
        aircraftHomeLocation = aircraftHomeLocationIN;
        aircraftHomeHeading = homeHeadingIN;

        aircraftLatLng = new LatLng(aircraftCurrentLocation.getLatitude(),
                aircraftCurrentLocation.getLongitude());
        homeLatLng = new LatLng(aircraftHomeLocation.getLatitude(),
                aircraftHomeLocation.getLongitude());
    }

    public double getAircraftLatitude () { return aircraftCurrentLocation.getLatitude(); }
    public double getAircraftLongitude () { return aircraftCurrentLocation.getLongitude(); }
    public float getAircraftAltitude () { return aircraftCurrentLocation.getAltitude(); }

    public double getAircraftPitch () { return aircraftCurrentAttitude.pitch; }
    public double getAircraftRoll () { return aircraftCurrentAttitude.roll; }

    /**
     * @return single pos/neg value with range +/- 180 in degrees clockwise from true north
     */
    public double getAircraftRawYaw() {
        return aircraftCurrentAttitude.yaw;
    }

    /**
     * @return double value of aircraft yaw from its home heading (pos/neg 180deg)
     */
    public double getAircraftHeadingRefHome() {
        return Calc.calcHeadingDifference(aircraftHomeHeading, getAircraftRawYaw());
    }

    public double getHomeLatitude () { return aircraftHomeLocation.getLatitude(); }
    public double getHomeLongitude () { return aircraftHomeLocation.getLongitude(); }
    public double getHomeHeading () { return aircraftHomeHeading;}

    /**
     * @return double value in meters of current position relative to home (from right of craft)
     */
    public XYValues getAircraftMeterOffsetFromHome() {
        return Calc.getXYOffsetBetweenPointsNormalToOriginHeading(
                homeLatLng, aircraftHomeHeading, aircraftLatLng);
    }

}
