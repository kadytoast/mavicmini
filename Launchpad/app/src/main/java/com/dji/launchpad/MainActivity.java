package com.dji.launchpad;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

/**
 * Main Activity for all student code -- should mostly interact with AircraftController only
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private AircraftController airC;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        airC = new AircraftController(this);

        airC.onCreate();
        loadUI();
    }

    // activity state methods DNT

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

    // method to initialize buttons and add to click listener
    private void loadUI() {
        /*
        // update button vars
        // class vars for buttons
        Button mHome = findViewById(R.id.btn_set_home);
        Button mTakeOff = findViewById(R.id.btn_take_off);
        Button mLand = findViewById(R.id.btn_land);

        // add to click listener
        mHome.setOnClickListener(this);
        mTakeOff.setOnClickListener(this);
        mLand.setOnClickListener(this);
        */

    }

    // onclick method for tracking button presses -- all should be in here
    @Override
    public void onClick(View view) {

    }
}
