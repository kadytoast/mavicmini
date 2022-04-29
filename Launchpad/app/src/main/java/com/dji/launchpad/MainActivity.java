package com.dji.launchpad;

import static java.lang.Math.abs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

/**
 * Main Activity for all student code -- should interact with AircraftController only
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private AircraftController air;
    public DebugClient debug;
    private ClickInterfaces ui;

    /**
     * necessary stuff dont touch! VVVVV
     */

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        air = new AircraftController(this);
        ui = new ClickInterfaces(this, air);

        air.onCreate();
        ui.onCreate();
        loadUI();
        debug = new DebugClient(this.getBaseContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        air.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResume () {
        super.onResume();
        air.onResume();
    }

    @Override
    public void onPause () {
        air.onPause();
        super.onPause();
    }

    @Override
    public void onStop () {
        air.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy () {
        air.onDestroy();
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
        findViewById(R.id.btn_startpath).setOnClickListener(this);
        findViewById(R.id.btn_stoppath).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_startpath:
                // write your primary flight code here, to be initiated after takeoff
                air.yawTo(0);
                air.yawTo(180);
                air.pauseFlight(5);
                air.yawTo(90);
                air.pauseFlight(5);
                air.yawTo(-90);
                air.pauseFlight(5);
                air.yawTo(0);
                break;

            case R.id.btn_stoppath:
                air.clearCurrentFlight();
                break;

            default:
                break;
        }
    }

}
