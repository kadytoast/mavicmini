package com.dji.launchpad;

import static com.dji.launchpad.PosUtils.Calc.calcHeadingDifference;
import com.dji.launchpad.PosUtils.XYValues;
import com.dji.launchpad.PosUtils.AircraftPositionalData;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import dji.common.error.DJISDKError;
import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
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
    public TextView mTextViewHome;

    private TimerTask mSendFlightDataTask = null;
    private Timer mSendFlightDataTimer = null;

    private TimerTask mCheckFlightPositionTask = null;
    private Timer mCheckFlightPositionTimer = null;

    /**
     * static flight control variables deprecated for FlightControlData and PID handler, but active
     */
    private float mPitch = 0;
    private float mRoll = 0;
    private float mYaw = 0;
    private float mThrottle = 0;

    private FlightControlData flightControlData = null;

    private LatLng mTargetFuturePosition = null;

    private double mHomeLat;
    private double mHomeLong;
    private double mAircraftHomeHeading;

    public double rth_default_height;

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


    // method that updates the textview with the current home position, as well as global vars
    void updateHomePos() {
        try {
            // call function with callback implementation
            mFlightController.getHomeLocation(
                    new CommonCallbacks.CompletionCallbackWith<LocationCoordinate2D>() {

                        // callback overrides
                        @Override
                        public void onSuccess(LocationCoordinate2D locationCoordinate2D) {
                            try {
                                mHomeLat = locationCoordinate2D.getLatitude();
                                mHomeLong = locationCoordinate2D.getLongitude();
                                mAircraftHomeHeading = getLocation().getAircraftYaw();

                                mTextViewHome.setText("Latitude : " + mHomeLat + "\nLongitude : " + mHomeLong + "\nAltitude: " +
                                        "Home ref to North : " + mAircraftHomeHeading);
                            }
                            catch (Exception e) {
                                ma.debug.errlog(e, "update home pos success");
                            }
                        }

                        @Override
                        public void onFailure(DJIError djiError) {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            }
                        }

                    }
            );
        }
        catch (Exception e) {
            ma.debug.errlog(e, "updatehomepos");
        }
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
                    mFlightController.setRollPitchControlMode(RollPitchControlMode.ANGLE);
                    mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
                    mFlightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
                    mFlightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
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
                XYValues offset = flight.getAircraftMeterOffsetFromHome();


                String pitch = String.format("%.2f", flight.getAircraftPitch());
                String roll = String.format("%.2f", flight.getAircraftRoll());
                String yaw = String.format("%.2f", flight.getAircraftHeadingRefHome());


                String positionX = String.format("%.2f", offset.X);
                String positionY = String.format("%.2f", offset.Y);
                String positionZ = String.format("%.2f", flight.getAircraftAltitude());

                // necessary components for the xyoffset func, readout for testing
                double hypotenuseDistance = SphericalUtil.computeDistanceBetween(flight.homeLatLng, flight.aircraftLatLng);
                double hypotenuseHeading = SphericalUtil.computeHeading(flight.homeLatLng, flight.aircraftLatLng);
                double rawHeadingDifference = calcHeadingDifference(flight.getHomeHeading(), hypotenuseHeading);

                String hypDis = String.format("%.2f", hypotenuseDistance);
                String hypHea = String.format("%.2f", hypotenuseHeading);
                String heaDif = String.format("%.2f", rawHeadingDifference);

                mTextViewPosition.setText(
                        "\nPitch : " + pitch + "\nRoll : " + roll + "\nYaw : " + yaw +
                                "\nPosX : " + positionX + "\nPosY : " + positionY + "\nPosZ : " + positionZ +
                                "\nHypDis : " + hypDis + "\nHypHea : " + hypHea + "\nheaDif : " + heaDif);

                LocalDateTime now = LocalDateTime.now();
                int secBetweenLogs = 2;
                // set of ifs to ensure values are only logged once in the 5 seconds (func is called 10x/s)
                if (now.getSecond() % secBetweenLogs != 0 && !mXYIfLogValues) {
                    mXYIfLogValues = true;
                }
                else if (now.getSecond() % secBetweenLogs == 0 && mXYIfLogValues) {
                    ma.debug.log("\nIN : \n" +
                            "origin lat = " + flight.homeLatLng.latitude + "\n" +
                            "origin lon = " + flight.homeLatLng.longitude + "\n" +
                            "originheading = " + mAircraftHomeHeading + "\n" +
                            "target lat = " + flight.aircraftLatLng.latitude + "\n" +
                            "target lon = " + flight.aircraftLatLng.longitude + "\n" +
                            "OUT : \n" +
                            "X = " + offset.X + "\n" +
                            "Y = " + offset.Y + "\n");
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
     * input float degree -180 to 180 to set pitch angle of craft
     * deprecated in lieu of FlightControlData and PID control
     */
    public void setPitch (float deg) {
        mPitch = deg;
        startFlightManagementTasks();
    }

    /**
     * input float degree -180 to 180 to set roll angle of craft
     * deprecated in lieu of FlightControlData and PID control
     */
    public void setRoll (float deg) {
        mRoll = deg;
        startFlightManagementTasks();
    }

    /**
     * input float velocity for deg/second rotation of craft on yaw axis
     * deprecated in lieu of FlightControlData and PID control
     */
    public void setYaw (float vel) {
        mYaw = vel;
        startFlightManagementTasks();
    }

    /**
     * input float value for target velocity on z axis (positive results in craft ascend)
     * deprecated in lieu of FlightControlData and PID control
     */
    public void setThrottle (float vel) {
        mThrottle = vel;
        startFlightManagementTasks();
    }

    /**
     * resets craft orientation on pitch roll yaw and throttle
     */
    public void resetAircraftOrientation () {
        mPitch = 0;
        mRoll = 0;
        mYaw = 0;
        mThrottle = 0;
        startFlightManagementTasks();
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
                mFlightControllerState.getAttitude(), new LocationCoordinate2D(mHomeLat, mHomeLong), mAircraftHomeHeading);
    }

    /**
     * set target future position value with LatLng class
     */
    public void setTargetFuturePosition (LatLng target) {
        mTargetFuturePosition = target;
    }

    /**
     * call to return to home location, pass height in meters for craft altitude while returning
     * to home (relative to takeoff location)
     */
    /*TODO write smarter return to home protocol, fly at specified altitude back to home point
        through cardinal directions and return to takeoff attitude (including yaw),
        because default RTH has minimum height of 20m, check with winter if this is wanted*/

    // TODO write indoor/outdoor toggle switch to set return to home height

    // TODO write forward/back method for basic movement in base app

    /*
     * API Control Methods ^^^^
     */



    public void startFlightManagementTasks() {
        // starting send flight data
        if (null == mSendFlightDataTimer) {
            mSendFlightDataTask = new sendFlightDataTask();
            mSendFlightDataTimer = new Timer();
            mSendFlightDataTimer.schedule(mSendFlightDataTask, 0, 100);
        }
        // starting check flight data
        if (null == mCheckFlightPositionTimer) {
            mCheckFlightPositionTask = new checkFlightPositionTask();
            mCheckFlightPositionTimer = new Timer();
            mCheckFlightPositionTimer.schedule(mCheckFlightPositionTask, 0, 50);
        }
    }

    public void killFlightManagementTasks() {
        // killing send flight data
        if (null != mSendFlightDataTimer) {
            mSendFlightDataTask.cancel();
            mSendFlightDataTask = null;
            mSendFlightDataTimer.cancel();
            mSendFlightDataTimer.purge();
            mSendFlightDataTimer = null;
        }
        // killing check flight data
        if (null != mCheckFlightPositionTimer) {
            mCheckFlightPositionTask.cancel();
            mCheckFlightPositionTask = null;
            mCheckFlightPositionTimer.cancel();
            mCheckFlightPositionTimer.purge();
            mCheckFlightPositionTimer = null;
        }
    }

    class sendFlightDataTask extends TimerTask {
        @Override
        public void run() {

            if (ifFlightController()) {
                // pass flight control data (set by pid handler), otherwise create new with zeroes
                if(flightControlData != null) {
                    mFlightController.sendVirtualStickFlightControlData(flightControlData,
                            djiError -> {}
                    );
                }
                else {
                    mFlightController.sendVirtualStickFlightControlData(
                            new FlightControlData(mPitch, mRoll, mYaw, mThrottle),
                            djiError -> {}
                    );
                }
            }
        }
    }

    class checkFlightPositionTask extends TimerTask {
        @Override
        public void run() {

            if (ifFlightController()) {
                // check position, act accordingly, use current targetpos class vars

            }
        }
    }

}
