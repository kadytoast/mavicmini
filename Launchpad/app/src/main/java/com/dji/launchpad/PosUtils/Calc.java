package com.dji.launchpad.PosUtils;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.time.LocalDateTime;

public class Calc {


    /**
     * static method to return XY position of target relative to origin and origin heading
     * @param origin LatLng object containing position of origin
     * @param originHeading double value containing origin's heading offset from north !! +/- 180 clockwise !!
     * @param target LatLng object containing position of target
     * @return XYvalues of cardinal offset of target from origin in meters
     */
    public static XYValues getXYOffsetBetweenPointsNormalToOriginHeading(LatLng origin, double originHeading, LatLng target) {
        // output values
        double x = 0;
        double y = 0;

        // x and y quadrant correction (set to 1 or -1 ONLY by correction conditionals)
        int xCorrector = 1;
        int yCorrector = 1;

        // calculate hypotenuse
        double hypotenuseDistance = SphericalUtil.computeDistanceBetween(origin, target);
        double hypotenuseHeading = SphericalUtil.computeHeading(origin, target);

        // raw heading difference
        double rawHeadingDifference = calcHeadingDifference(originHeading, hypotenuseHeading);
        double absHeadingDifference = abs(rawHeadingDifference);

        // heading difference calculations to normalize for different quadrants
        /* P = positive, N = negative
            NP    |    PP
                  ^
          ----- origin -----
                  |
            NN    |    PN
         */
        // if rawheading dif angle is negative, negative xCorrector (Nx quadrants)
        if (rawHeadingDifference < 0) {
            xCorrector = -1;
        }
        // if heading difference is more than 90, negative yCorrector (xN quadrants)
        if (absHeadingDifference > 90) {
            yCorrector = -1;
            absHeadingDifference -= 90;
        }
        // otherwise defaults (PP quadrant)

        double theta = 90 - absHeadingDifference;
        x = xCorrector * (sin(toRadians(theta)) * hypotenuseDistance);
        y = yCorrector * (cos(toRadians(theta)) * hypotenuseDistance);

        return new XYValues(x, y);
    }

    /**
     * @param baseRef the heading to reference second heading from (+/- 180)
     * @param secRef the heading to calculate offset of from baseRef (+/- 180)
     * @return double value of difference between two headings that are (originally) referenced to true north
     * return is within +/- 180 to have full range and denote quadrant
     */
    public static double calcHeadingDifference(double baseRef, double secRef) {

        if (baseRef < 0) {
            baseRef += 360;
        }
        if (secRef < 0) {
            secRef += 360;
        }

        // both refs are now single positive value 0-359, subtract
        double finalOut = baseRef - secRef;

        // ensure val is negative if left of baseref and positive if right of baseref
        finalOut = abs(finalOut);
        if (baseRef > secRef) {
            finalOut *= -1;
        }

        // set finalout back to +/- 180 value
        if (finalOut > 180) {
            finalOut -= 360;
        }
        if (finalOut < -180) {
            finalOut += 360;
        }

        return finalOut;
    }

}
