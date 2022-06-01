package com.dji.launchpad;

import static java.lang.Math.abs;

import com.dji.launchpad.Utils.Calc;
import com.dji.launchpad.Utils.FlightQueue;
import com.dji.launchpad.Utils.FlightQueueData;
import com.dji.launchpad.Utils.XYValues;
import com.dji.launchpad.Utils.AircraftPositionalData;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJISDKError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightOrientationMode;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.common.error.DJIError;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;


public class AircraftController {

    private static final String TAG = AircraftController.class.getName();

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;

    public FlightController mFlightController;
    public FlightControllerState mFlightControllerState;
    protected TextView mConnectStatusTextView;

    public TextView mTextViewPosition;

    private TimerTask mSendFlightDataTask = null;
    private Timer mSendFlightDataTimer = null;
    private boolean mTaskRunning = false;

    private FlightQueue mFlightQueue;

    private float mPitch = 0;
    private float mRoll = 0;
    private float mYaw = 0;
    private float mThrottle = 0;
    private double mFlightEndTime = 0;
    private LocalDateTime mFlightStartTime = null;

    private static boolean mXYIfLogValues = true;

    private final MainActivity ma;

    public AircraftController (MainActivity maIN) {
        ma = maIN;
    }

    protected void onCreate() {
        checkAndRequestPermissions();

        // Register the broadcast receiver for receiving the device connection's changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(AircraftObjHandler.FLAG_CONNECTION_CHANGE);
        ma.registerReceiver(mReceiver, filter);
        mFlightQueue = new FlightQueue(ma);
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(ma, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (!missingPermission.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(ma,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    /**
     * Result of runtime permission request
     */
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    showToast( "registering, pls wait...");
                    DJISDKManager.getInstance().registerApp(ma.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                DJILog.e("App registration", DJISDKError.REGISTRATION_SUCCESS.getDescription());
                                DJISDKManager.getInstance().startConnectionToProduct();
                                showToast("Register Success");
                            } else {
                                showToast( "Register sdk fails, check network is available");
                            }
                            Log.v(TAG, djiError.getDescription());
                        }

                        @Override
                        public void onProductDisconnect() {
                            Log.d(TAG, "onProductDisconnect");
                            showToast("Product Disconnected");

                        }
                        @Override
                        public void onProductConnect(BaseProduct baseProduct) {
                            Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                            showToast("Product Connected");

                        }

                        @Override
                        public void onProductChanged(BaseProduct baseProduct) {

                        }

                        @Override
                        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                      BaseComponent newComponent) {

                            if (newComponent != null) {
                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                    }
                                });
                            }
                            Log.d(TAG,
                                    String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                            componentKey,
                                            oldComponent,
                                            newComponent));

                        }
                        @Override
                        public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

                        }

                        @Override
                        public void onDatabaseDownloadProgress(long l, long l1) {

                        }
                    });
                }
            });
        }
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTitleBar();
        }
    };

    public void showToast(final String msg) {
        ma.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(ma, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void updateTitleBar() {
        try {
            if (mConnectStatusTextView == null) return;
            boolean ret = false;
            BaseProduct product = AircraftObjHandler.getProductInstance();
            if (product != null) {
                if (product.isConnected()) {
                    //The product is connected
                    mConnectStatusTextView.setText(AircraftObjHandler.getProductInstance().getModel() + " Connected");
                    ret = true;
                } else {
                    if (product instanceof Aircraft) {
                        Aircraft aircraft = (Aircraft) product;
                        if (aircraft.getRemoteController() != null && aircraft.getRemoteController().isConnected()) {
                            // The product is not connected, but the remote controller is connected
                            mConnectStatusTextView.setText("only RC Connected");
                            ret = true;
                        }
                    }
                }
            }

            if (!ret) {
                // The product or the remote controller are not connected.
                mConnectStatusTextView.setText("Disconnected");
            }
        }
        catch (Exception e) {
            ma.debug.errlog(e, "updatetitlebar");
        }
    }


    public void onResume() {
        Log.e(TAG, "onResume");
        updateTitleBar();
        initFlightController();

    }

    public void onPause() {
        Log.e(TAG, "onPause");
    }

    public void onStop() {
        Log.e(TAG, "onStop");
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        ma.finish();
    }

    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        ma.unregisterReceiver(mReceiver);
        killFlightManagementTasks();
    }

    void initFlightController() {
        try {
            Aircraft aircraft = AircraftObjHandler.getAircraftInstance();
            if (aircraft == null || !aircraft.isConnected()) {
                //showToast("Disconnected");
                mFlightController = null;
                mFlightControllerState = null;
            } else {
                // reinit flight controller only if needed
                if (mFlightController == null) {
                    mFlightController = aircraft.getFlightController();
                    mFlightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
                    mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
                    mFlightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
                    mFlightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
                    mFlightController.setVirtualStickAdvancedModeEnabled(true);
                    mFlightQueue.clearFlightData();
                }

                setFlightControllerStateCallback();
            }
        }
        catch (Exception e) {
            ma.debug.errlog(e, "initflightcontroller");
        }
    }

    private void setFlightControllerStateCallback() {
        // reload state callback and ui updates
        mFlightController.setStateCallback(stateData -> {
            try {
                mFlightControllerState = stateData;
                AircraftPositionalData flight = getLocation();

                String pitch = String.format("%.3f", flight.getAircraftPitch());
                String roll = String.format("%.3f", flight.getAircraftRoll());
                String yaw = String.format("%.3f", flight.getAircraftHeading());


                mTextViewPosition.setText(
                        "\nPitch : " + pitch + "\nRoll : " + roll + "\nYaw : " + yaw );

                LocalDateTime now = LocalDateTime.now();
                int secBetweenLogs = 2;
                // set of ifs to ensure values are only logged once in the 5 seconds (func is called 10x/s)
                if (now.getSecond() % secBetweenLogs != 0 && !mXYIfLogValues) {
                    mXYIfLogValues = true;
                }
                else if (now.getSecond() % secBetweenLogs == 0 && mXYIfLogValues) {
                    ma.debug.log("\n FLIGHTVALS \n" +
                            "Pitch: " + mPitch + "\n" +
                            "Roll: " + mRoll + "\n" +
                            "Yaw: " + mYaw + "\n" +
                            "Throttle: " + mThrottle + "\n" +
                            "Task Length: " + mFlightEndTime + "\n");
                    mXYIfLogValues = false;
                }
            }
            catch (Exception e) {
                ma.debug.errlog(e, "fc state callback");
            }
        });
    }

    /*
     * API Control Methods VVVVV
     */
    /**
     * constructs and adds flight data object to queue
     * @param pitch float velocity +/- forward/backward 15 m/s
     * @param roll float velocity +/- starboard/port 15 m/s
     * @param yaw float angle +/- right/left 100 deg/s
     * @param throttle float max 4 m/s
     * @param time double less than 10 seconds
     */
    public void addTask (float pitch, float roll, float yaw, float throttle, double time) {
        if (ifFlightController()) {
            startFlightManagementTasks();
            mFlightQueue.addFlightData(pitch, roll, yaw, throttle, time);
        }
    }

    /**
     * @param velocity to hold while pitching forward, max 15 m/s
     * @param time seconds to travel at this velocity
     */
    public void flyForward(float velocity, double time) {
        if (ifFlightController()) {
            startFlightManagementTasks();
            velocity = abs(velocity);
            mFlightQueue.addFlightData(velocity, 0, 0, 0, time);
        }
    }

    /**
     * @param velocity to hold while pitching backward, max 15 m/s
     * @param time seconds to travel at this velocity
     */
    public void flyBackward(float velocity, double time) {
        if (ifFlightController()) {
            startFlightManagementTasks();
            velocity = -1 * (abs(velocity));
            mFlightQueue.addFlightData(velocity, 0, 0, 0, time);
        }

    }

    /**
     * @param velocity to hold while rolling left, max 15 m/s
     * @param time seconds to travel at this velocity
     */
    public void flyPort(float velocity, double time) {
        if (ifFlightController()) {
            startFlightManagementTasks();
            velocity = -1 * (abs(velocity));
            mFlightQueue.addFlightData(0, velocity, 0, 0, time);
        }
    }

    /**
     * @param velocity to hold while rolling to right of craft, max 15 m/s
     * @param time seconds to travel at this velocity
     */
    public void flyStarboard(float velocity, double time) {
        if (ifFlightController()) {
            startFlightManagementTasks();
            velocity = abs(velocity);
            mFlightQueue.addFlightData(0, velocity, 0, 0, time);
        }
    }

    /**
     * @param velocity to yaw in degrees per second, max 100
     * @param time seconds to yaw at this velocity
     */
    public void yawRight (float velocity, double time) {
        if (ifFlightController()) {
            startFlightManagementTasks();
            velocity = abs(velocity);
            mFlightQueue.addFlightData(0, 0, velocity, 0, time);
        }
    }

    /**
     * @param velocity to yaw in degrees per second, max 100
     * @param time seconds to yaw at this velocity
     */
    public void yawLeft (float velocity, double time) {
        if (ifFlightController()) {
            startFlightManagementTasks();
            velocity = -1 * abs(velocity);
            mFlightQueue.addFlightData(0, 0, velocity, 0, time);
        }
    }

    /**
     * @param velocity +/- 4 m/s up or down, respectively
     * @param time seconds to travel at this velocity
     */
    public void throttleFor (float velocity, double time) {
        if (ifFlightController()) {
            startFlightManagementTasks();
            mFlightQueue.addFlightData(0, 0, 0, velocity, time);
        }
    }

    /**
     * @param time to pause flight for, in seconds
     */
    public void pauseFlight (double time) {
        if (ifFlightController()) {
            startFlightManagementTasks();
            mFlightQueue.addFlightData(0, 0, 0, 0, time);
        }
    }

    /**
     * launches the aircraft, not queue synced
     */
    public void takeOff () {
        if (ifFlightController()){
            showToast("Takeoff Started");
            mFlightController.startTakeoff(
                    djiError -> {
                        if (djiError != null) {
                            showToast(djiError.getDescription());
                        } else {
                            showToast("Takeoff Complete");
                        }
                    }
            );
        }
    }

    /**
     * lands the aircraft, not queue synced
     */
    public void land() {
        if (ifFlightController()){
            showToast("Landing Started");
            mFlightController.startLanding(
                    djiError -> {
                        if (djiError != null) {
                            showToast(djiError.getDescription());
                        } else {
                            showToast("Start Landing");
                        }
                    }
            );
        }
    }

    /**
     * kills tasks related to controlling flight over queue, reset values
     */
    public void killFlightTasks () {
        if (ifFlightController()) {
            startFlightManagementTasks();
            mFlightQueue.clearFlightData();
            mFlightStartTime = null;
            mPitch = 0;
            mRoll = 0;
            mYaw = 0;
            mThrottle = 0;
        }
        mTaskRunning = false;
        killFlightManagementTasks();
    }

    /**
     * checks to ensure a valid flight controller object is active, use before interaction
     */
    public boolean ifFlightController() {
        return mFlightController != null;
    }

    /**
     * returns object containing latitude, longitude, altitude of craft, and home latitude and longitude
     * altitude is relative to home position
     */
    public synchronized AircraftPositionalData getLocation () {
        return new AircraftPositionalData(mFlightControllerState.getAircraftLocation(),
                mFlightControllerState.getAttitude());
    }

    private void startFlightManagementTasks() {
        // starting send flight data
        if (null == mSendFlightDataTimer) {
            mSendFlightDataTask = new sendFlightDataTask();
            mSendFlightDataTimer = new Timer();
            mSendFlightDataTimer.schedule(mSendFlightDataTask, 0, 100);
            mFlightQueue.clearFlightData();
            mFlightStartTime = null;
        }

    }

    private void killFlightManagementTasks() {
        // killing send flight data
        if (null != mSendFlightDataTimer) {
            mSendFlightDataTask.cancel();
            mSendFlightDataTask = null;
            mSendFlightDataTimer.cancel();
            mSendFlightDataTimer.purge();
            mSendFlightDataTimer = null;
        }
    }

    class sendFlightDataTask extends TimerTask {
        @Override
        public void run() {
            if (ifFlightController()) {
                FlightQueueData flightData;
                if (!mTaskRunning) {
                    flightData = mFlightQueue.getNextFlightData();
                    mTaskRunning = true;
                    if (flightData != null) {
                        // task values
                        mFlightStartTime = LocalDateTime.now();
                        mFlightEndTime = flightData.getResetTime();
                        mPitch = flightData.getPitch();
                        mRoll = flightData.getRoll();
                        mYaw = flightData.getYaw();
                        mThrottle = flightData.getThrottle();
                        ma.debug.log("\nflightdata is valid in send flight data\n");
                    }
                    else {
                        // idle values
                        mFlightStartTime = null;
                        mFlightEndTime = 0;
                        mPitch = 0;
                        mRoll = 0;
                        mYaw = 0;
                        mThrottle = 0;
                        TextView queue = ma.findViewById(R.id.textview_flightqueue);
                        queue.setText("task null, queue empty");
                        //ma.debug.log("flightdata is null in send flight data");
                    }
                }
                else {
                    try {
                        // checks if (start time) is less than or equal to (current time minus endtime), returns -1 or 0 if so
                        if (mFlightStartTime.compareTo(LocalDateTime.now().minusSeconds((long) mFlightEndTime)) <= 0) {
                            // reset flight values for next task
                            mFlightStartTime = null;
                            mTaskRunning = false;
                            //ma.debug.log("out of time check in send flight data");
                        }
                    }
                    catch (NullPointerException e) {
                        // queue has another item, stop idle and start next task
                        if (!mFlightQueue.isQueueEmpty()){
                            mFlightStartTime = null;
                            mTaskRunning = false;
                            //ma.debug.errlog(e, "null catch and queue not empty in send flight data");
                        }
                        //ma.debug.errlog(e, "null catch in send flight data");
                    }
                }
                // send data regardless
                mFlightController.sendVirtualStickFlightControlData(
                        // shouldve been pitch first and roll second, but this is the only way it worked?
                        new FlightControlData(mRoll, mPitch, mYaw, mThrottle), djiError -> {});
            }
        }
    }

}
