package com.dji.launchpad;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Main Activity for all student code -- should mostly interact with AircraftController only
 */
public class MainActivity extends Activity implements View.OnClickListener {

    // class vars for buttons
    private Button mHome;
    private Button mTakeOff;
    private Button mLand;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadUI();
    }

    // method to initialized buttons and add to click listener
    private void loadUI() {
        // update button vars

        // add to click listener

    }

    // onclick method for tracking button presses -- all should be in here
    @Override
    public void onClick(View view) {

    }
}
