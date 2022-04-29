package com.dji.launchpad.Utils;

import com.google.android.gms.maps.model.LatLng;

import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.model.LocationCoordinate2D;

public class AircraftPositionalData {

    private final LocationCoordinate3D aircraftCurrentLocation;
    private final Attitude aircraftCurrentAttitude;
    private final LocationCoordinate2D aircraftHomeLocation;
    private final double aircraftHomeHeading;
    private final LatLng aircraftLatLng;
    private final LatLng homeLatLng;

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

    /**
     * @return current aircraft lat/long
     */
    public LatLng getAircraftLatLng () { return aircraftLatLng; }

    /**
     * @return aircraft's home position
     */
    public LatLng getHomeLatLng () { return homeLatLng;}

    /**
     * @return current aircraft altitude
     */
    public float getAircraftAltitude () { return aircraftCurrentLocation.getAltitude(); }

    /**
     * @return current aircraft pitch in +/- degrees
     */
    public double getAircraftPitch () { return aircraftCurrentAttitude.pitch; }

    /**
     * @return current aircraft roll in +/- degrees
     */
    public double getAircraftRoll () { return aircraftCurrentAttitude.roll; }

    /**
     * @return single pos/neg value with range +/- 180 in degrees clockwise from true north
     */
    public double getAircraftHeading() {
        return aircraftCurrentAttitude.yaw;
    }

    /**
     * @return double value of aircraft yaw from its home heading (pos/neg 180deg)
     */
    public double getAircraftHeadingRefHome() {
        return Calc.calcHeadingDifference(aircraftHomeHeading, getAircraftHeading());
    }

    /**
     * @return double value in meters of current XY position relative to home
     * (positive xy is forward-right of home)
     */
    public XYValues getAircraftMeterOffsetFromHome() {
        return Calc.getXYOffsetBetweenPointsNormalToOriginHeading(
                homeLatLng, aircraftHomeHeading, aircraftLatLng);
    }

}
