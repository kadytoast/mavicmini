package com.dji.launchpad;

import static java.lang.Math.abs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

/**
 * Main Activity for all student code -- should interact with AircraftController only
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private AircraftController air;
    public DebugClient debug;
    private ClickInterfaces ui;
    private String b1_name;
    private String b2_name;
    private String b3_name;

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
        findViewById(R.id.btn_killtasks).setOnClickListener(this);
        findViewById(R.id.btn_startpath_1).setOnClickListener(this);
        findViewById(R.id.btn_startpath_2).setOnClickListener(this);
        findViewById(R.id.btn_startpath_3).setOnClickListener(this);
        //TODO add button listeners for new buttons here

        // text assignment for buttons
        b1_name = "short cardinal path"; // set custom name for path in this file for ease
        Button b1 = (Button) findViewById(R.id.btn_startpath_1);
        b1.setText(b1_name);

        b2_name = "turn test 45d/s 360";
        Button b2 = (Button) findViewById(R.id.btn_startpath_2);
        b2.setText(b2_name);

        b3_name = "combo diag/curve test";
        Button b3 = (Button) findViewById(R.id.btn_startpath_3);
        b3.setText(b3_name);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_startpath_1:
                //TODO write your paths out here, to be activated on button press
                debug.log(b1_name + " started");
                air.flyForward(0.75f, 2);
                air.flyBackward(0.75F, 2);
                air.throttleFor(0.75F, 2);
                air.pauseFlight(1);
                air.throttleFor(-0.75F, 2);
                air.flyPort(0.75F, 2);
                air.flyStarboard(0.75F, 4);
                air.flyPort(0.75F, 2);
                break;

            case R.id.btn_startpath_2:
                debug.log(b2_name + " started");
                air.yawLeft(45, 2); // 90 left
                air.pauseFlight(1);
                air.yawLeft(45, 2); // 90 left
                air.pauseFlight(1);
                air.yawRight(45, 2); // 90 right
                air.yawLeft(45, 4); // 180 left
                air.pauseFlight(1);
                air.yawLeft(45, 2); // 90 left
                break;

            case R.id.btn_startpath_3:
                debug.log(b3_name + " started");
                air.addTask(1f, 1f, 0f, 0f, 1); // f/r diag
                air.pauseFlight(1);
                air.addTask(-1f, -1f, 0f, 0f, 1); // b/l diag
                air.pauseFlight(1);
                air.addTask(.5f, 0f, 30f, 0f, 12); // circle?
                break;

            //TODO add cases for new buttons here

            case R.id.btn_killtasks:
                air.killFlightTasks();
                TextView queue = findViewById(R.id.textview_flightqueue);
                queue.setText("tasks killed, queue empty");
                break;

            default:
                break;
        }
    }

}
