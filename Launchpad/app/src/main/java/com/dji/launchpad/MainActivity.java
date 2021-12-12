package com.dji.launchpad;

import static java.lang.Math.abs;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

/**
 * Main Activity for all student code -- should interact with AircraftController only
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private AircraftController airC;

    /**
     * necessary stuff dont touch! VVVVV
     */

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        airC = new AircraftController(this);

        airC.onCreate();
        loadUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        airC.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResume () {
        super.onResume();
        airC.onResume();
    }

    @Override
    public void onPause () {
        airC.onPause();
        super.onPause();
    }

    @Override
    public void onStop () {
        airC.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy () {
        airC.onDestroy();
        super.onDestroy();
    }

    /**
     * necessary stuff dont touch! ^^^^^
     */

    /**
     * your workspace VVVVV
     */

    // method to initialize buttons and add to click listener
    private void loadUI() {
        findViewById(R.id.btn_fly_forward_1m).setOnClickListener(this);
        findViewById(R.id.btn_fly_backward_1m).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        /*
        switch (view.getId()) {

            case R.id.btn_fly_forward_1m:
                if (airC.ifFlightController()) {
                    // reset attitude before starting
                    airC.resetAircraftOrientation();

                    // get current data, compute new lat long, then update class targ val
                    AircraftController.AircraftPositionalData locData = airC.getLocation();
                    LatLng curLatLng = new LatLng(locData.getAircraftLatitude(),
                            locData.getAircraftLongitude());
                    double curHeading = locData.getAircraftYaw();
                    double targOffset = 1;
                    airC.setmTargetFuturePosition(SphericalUtil.computeOffset(curLatLng, targOffset, curHeading));

                    // set pitch to fly forward, management task should stop when point is reached
                    airC.setPitch(-3);
                }
                break;

            case R.id.btn_fly_backward_1m:
                if (airC.ifFlightController()) {
                    // reset attitude before starting
                    airC.resetAircraftOrientation();

                    // get current data, compute new lat long, then update class targ val
                    AircraftController.AircraftPositionalData locData = airC.getLocation();
                    LatLng curLatLng = new LatLng(locData.getAircraftLatitude(),
                            locData.getAircraftLongitude());
                    double curHeading = abs(locData.getAircraftYaw() - 360); // with opposite heading for backwards
                    double targOffset = 1;
                    airC.setmTargetFuturePosition(SphericalUtil.computeOffset(curLatLng, targOffset, curHeading));

                    // set pitch to fly backward, management task should stop when point is reached
                    airC.setPitch(3);
                }
                break;
        }
        */
    }

}
