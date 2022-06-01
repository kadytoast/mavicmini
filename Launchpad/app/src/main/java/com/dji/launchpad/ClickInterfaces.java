package com.dji.launchpad;

import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.regex.Pattern;

public class ClickInterfaces implements View.OnClickListener{

    private MainActivity ma;
    private AircraftController air;

    private boolean mVirtualStickControlState = false;
    private boolean mTakeoffEnabledState = false;

    public ClickInterfaces(MainActivity maIN, AircraftController airIN) {
        ma = maIN;
        air = airIN;
    }

    public void showToast(final String msg) {
        ma.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(ma, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onCreate () {
        initUI();
    }

    private void initUI() {
        // sets up click listener for buttons and global textviews
        // variable definitions for buttons and textviews
        Button mBtnTakeOff = ma.findViewById(R.id.btn_take_off);
        Button mBtnLand = ma.findViewById(R.id.btn_land);

        // regular button listener for <onClick> method
        mBtnTakeOff.setOnClickListener(this);
        mBtnLand.setOnClickListener(this);
        ma.findViewById(R.id.btn_reload).setOnClickListener(this);
        ma.findViewById(R.id.btn_debugenter).setOnClickListener(this);

        mTakeoffEnabledState = false;
        mBtnTakeOff.setVisibility(Button.INVISIBLE);
        mBtnLand.setVisibility(Button.INVISIBLE);

        ToggleButton mTogVirtualSticks = ma.findViewById(R.id.tog_virtual_sticks);
        ToggleButton mTogTakeoffEnable = ma.findViewById(R.id.tog_takeoff_enable);
        air.mTextViewPosition = (TextView) ma.findViewById(R.id.textview_position);
        air.mConnectStatusTextView = (TextView) ma.findViewById(R.id.ConnectStatusTextView);

        // toggle button for virtual flight control enable/disable
        mTogVirtualSticks.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) { // virtual sticks enabled
                if (air.ifFlightController() && !mVirtualStickControlState) {
                    air.mFlightController.setVirtualStickModeEnabled(true, djiError -> {
                        if (djiError != null) {
                            // show error and flip back checked if failed
                            showToast(djiError.getDescription());
                            mTogVirtualSticks.setChecked(false);
                        }
                        else {
                            // show toast and updated control
                            mVirtualStickControlState = true;
                            showToast("Virtual Sticks Enabled");
                        }
                    });
                }
                // if flightcontroller is null, set to false
                else {
                    mTogVirtualSticks.setChecked(false);
                }
            }
            else { // virtual sticks disabled
                if (air.ifFlightController() && mVirtualStickControlState) {
                    air.mFlightController.setVirtualStickModeEnabled(false, djiError -> {
                        if (djiError != null) {
                            // show error and flip back checked if failed
                            showToast(djiError.getDescription());
                            mTogVirtualSticks.setChecked(true);
                        }
                        else {
                            // show toast and updated control
                            mVirtualStickControlState = false;
                            showToast("Virtual Sticks Disabled");
                            air.killFlightTasks();
                        }
                    });
                }
                // if flight controller is null, set to false
                else {
                    mTogVirtualSticks.setChecked(false);
                }
            }
        });

        mTogTakeoffEnable.setOnCheckedChangeListener(((compoundButton, b) -> {
            if (b) {
                mTakeoffEnabledState = true;
                mBtnTakeOff.setVisibility(Button.VISIBLE);
                mBtnLand.setVisibility(Button.VISIBLE);
            }
            else {
                mTakeoffEnabledState = false;
                mBtnTakeOff.setVisibility(Button.INVISIBLE);
                mBtnLand.setVisibility(Button.INVISIBLE);
            }
        }));

    }

    // default buttons
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_take_off:
                air.takeOff();
                break;

            case R.id.btn_land:
                air.land();
                break;

            case R.id.btn_reload:
                try {
                    Button reload = ma.findViewById(R.id.btn_reload);
                    reload.setText("...");

                    air.updateTitleBar();
                    air.initFlightController();

                    Handler awaitReload = new Handler();
                    awaitReload.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reload.setText("reload");
                        }
                    }, 500);
                }
                catch (Exception e) {
                    ma.debug.errlog(e, "btn_reload");
                }
                break;

            case R.id.btn_debugenter:
                System.out.println("debugenter pressed");
                EditText entry = ma.findViewById(R.id.editText_debugaddress);
                String address = entry.getText().toString();
                System.out.println("address content " + address);

                try {
                    if (!address.isEmpty()) {
                        // get and validate ip address
                        String ip = address.substring(0, address.indexOf(':'));
                        String zeroTo255
                                = "(\\d{1,2}|(0|1)\\"
                                + "d{2}|2[0-4]\\d|25[0-5])";
                        String regex
                                = zeroTo255 + "\\."
                                + zeroTo255 + "\\."
                                + zeroTo255 + "\\."
                                + zeroTo255;
                        boolean ifip = Pattern.matches(regex, ip);
                        System.out.println(ifip);

                        // get and validate port number
                        String port = address.substring(address.indexOf(':') + 1);
                        boolean ifport = (0 < Integer.parseInt(port) && Integer.parseInt(port) <= 65535);
                        System.out.println(ifport);

                        if (ifip && ifport) {
                            ma.debug.setPath(ip, port);
                            ma.debug.log("OPEN - Opened Debugger, App Version: " + R.string.version);
                        }
                    }

                }
                catch (Error e){
                    System.out.println("debugenter button caught : " + e.getMessage());
                }

                break;

            default:
                break;
        }
    }
}
