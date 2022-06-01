package com.dji.launchpad.Utils;

import com.google.android.gms.maps.model.LatLng;

import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.model.LocationCoordinate2D;

public class AircraftPositionalData {

    private final LocationCoordinate3D aircraftCurrentLocation;
    private final Attitude aircraftCurrentAttitude;
    private final LatLng aircraftLatLng;

    public AircraftPositionalData (LocationCoordinate3D aircraftCurrentLocationIN,
                                   Attitude aircraftCurrentAttitudeIN) {
        // define class vars
        aircraftCurrentLocation = aircraftCurrentLocationIN;
        aircraftCurrentAttitude = aircraftCurrentAttitudeIN;

        aircraftLatLng = new LatLng(aircraftCurrentLocation.getLatitude(),
                aircraftCurrentLocation.getLongitude());
    }

    /**
     * @return current aircraft lat/long
     */
    public LatLng getAircraftLatLng () { return aircraftLatLng; }

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


}
