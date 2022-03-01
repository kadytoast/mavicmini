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
     * @return double value in meters between origin point and target point with header
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
        double correctedHeadingDifference = abs(rawHeadingDifference);

        // heading difference calculations to normalize for different quadrants
        /* quadrant notations [0] = x, [1] = y, P = positive, N = negative
            NP    |    PP
                  ^
          ----- drone -----
                  |
            NN    |    PN
         */
        // if rawheading dif angle is negative (raw), negative xCorrector (Nx quadrants)
        if (rawHeadingDifference < 0) {
            xCorrector = -1;
        }
        // if heading difference is more than 90, negative yCorrector (xN quadrants)
        if (correctedHeadingDifference > 90) {
            yCorrector = -1;
            correctedHeadingDifference -= 90;
        }
        // otherwise defaults (PP quadrant)

        x = xCorrector * (sin(toRadians(correctedHeadingDifference)) * hypotenuseDistance);
        y = yCorrector * (cos(toRadians(correctedHeadingDifference)) * hypotenuseDistance);

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

        // both refs are now single positive value 0-359

        double finalOut = baseRef - secRef;

        finalOut = abs(finalOut);

        if (baseRef < secRef) {
            finalOut *= -1;
        }

        // set finalout back to +/- 180 value
        if (finalOut > 180) {
            finalOut -= 360;
            finalOut *= -1;
        }
        if (finalOut < -180) {
            finalOut += 360;
            finalOut *= -1;
        }

        return finalOut;
    }

}
