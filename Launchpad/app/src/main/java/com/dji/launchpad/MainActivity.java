package com.dji.launchpad;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

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
        // findViewById().setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // switch (view.getId()) {}
            // case (button id):
    }

    /**
     * sets all aircraft position values to zero, cancels current movements
     */
    private void setCraftFlat() {

    }
}
