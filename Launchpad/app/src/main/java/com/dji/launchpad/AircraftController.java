package com.dji.launchpad;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

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

public class AircraftController implements View.OnClickListener {

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

    private FlightController mFlightController;
    public FlightControllerState mFlightControllerState;
    protected TextView mConnectStatusTextView;

    private TextView mTextViewPosition;
    private TextView mTextViewHome;

    private TimerTask mSendFlightDataTask = null;
    private Timer mSendFlightDataTimer = null;

    private TimerTask mCheckFlightPositionTask = null;
    private Timer mCheckFlightPositionTimer = null;

    private float mPitch = 0;
    private float mRoll = 0;
    private float mYaw = 0;
    private float mThrottle = 0;
    private boolean mVirtualStickControlState = false;
    private boolean mTakeoffEnabledState = false;

    private LatLng mTargetFuturePosition = null;

    private double homeLat;
    private double homeLong;
    private double aircraftHomeHeading;
    private float homeAlt_refSeaLevel;

    public double rth_default_height;

    private final MainActivity ma;

    public AircraftController (MainActivity maIN) {
        ma = maIN;
    }

    protected void onCreate() {
        checkAndRequestPermissions();

        initUI();

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

    private void updateTitleBar() {
        if(mConnectStatusTextView == null) return;
        boolean ret = false;
        BaseProduct product = AircraftObjHandler.getProductInstance();
        if (product != null) {
            if(product.isConnected()) {
                //The product is connected
                mConnectStatusTextView.setText(AircraftObjHandler.getProductInstance().getModel() + " Connected");
                ret = true;
            } else {
                if(product instanceof Aircraft) {
                    Aircraft aircraft = (Aircraft)product;
                    if(aircraft.getRemoteController() != null && aircraft.getRemoteController().isConnected()) {
                        // The product is not connected, but the remote controller is connected
                        mConnectStatusTextView.setText("only RC Connected");
                        ret = true;
                    }
                }
            }
        }

        if(!ret) {
            // The product or the remote controller are not connected.
            mConnectStatusTextView.setText("Disconnected");
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


    // method that updates the textview with the current home position
    private void updateHomePos () {
        // call function with callback implementation
        mFlightController.getHomeLocation(new CommonCallbacks.CompletionCallbackWith<LocationCoordinate2D>() {

            // callback overrides
            @Override
            public void onSuccess(LocationCoordinate2D locationCoordinate2D) {
                homeLat = locationCoordinate2D.getLatitude();
                homeLong = locationCoordinate2D.getLongitude();
                homeAlt_refSeaLevel = mFlightControllerState.getTakeoffLocationAltitude();
                aircraftHomeHeading = getLocation().getAircraftYaw();

                mTextViewHome.setText("Latitude : " + homeLat + "\nLongitude : " + homeLong + "\nAltitude: " +
                        homeAlt_refSeaLevel);
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

    private void initFlightController() {

        Aircraft aircraft = AircraftObjHandler.getAircraftInstance();
        if (aircraft == null || !aircraft.isConnected()) {
            //showToast("Disconnected");
            mFlightController = null;
            mFlightControllerState = null;
        } else {
            mFlightController = aircraft.getFlightController();
            mFlightController.setRollPitchControlMode(RollPitchControlMode.ANGLE);
            mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
            mFlightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
            mFlightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

            mFlightController.setStateCallback(stateData -> {

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
                        "\nPosX : " + positionX + "\nPosY : "  + positionY + "\nPosZ : " + positionZ +
                        "\nHypDis : " + hypDis + "\nHypHea : " + hypHea + "\nheaDif : " + heaDif);
            });
        }
    }

    private void initUI() {
        // sets up click listener for buttons and global textviews
        // variable definitions for buttons and textviews
        Button mBtnTakeOff = (Button) ma.findViewById(R.id.btn_take_off);
        Button mBtnLand = (Button) ma.findViewById(R.id.btn_land);
        Button mBtnReset = ma.findViewById(R.id.btn_set_craft_flat);
        Button mBtnSetHome = (Button) ma.findViewById(R.id.btn_set_home);
        Button mBtnGoHome = ma.findViewById(R.id.btn_rth);
        Button mBtnReload = ma.findViewById(R.id.btn_reload);

        // regular button listener for <onClick> method
        mBtnTakeOff.setOnClickListener(this);
        mBtnLand.setOnClickListener(this);
        mBtnSetHome.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);
        mBtnGoHome.setOnClickListener(this);
        mBtnReload.setOnClickListener(this);

        mTakeoffEnabledState = false;
        mBtnTakeOff.setVisibility(Button.INVISIBLE);
        mBtnLand.setVisibility(Button.INVISIBLE);

        ToggleButton mTogVirtualSticks = ma.findViewById(R.id.tog_virtual_sticks);
        ToggleButton mTogTakeoffEnable = ma.findViewById(R.id.tog_takeoff_enable);
        mTextViewPosition = (TextView) ma.findViewById(R.id.textview_position);
        mTextViewHome = ma.findViewById(R.id.textview_homecoords);
        mConnectStatusTextView = (TextView) ma.findViewById(R.id.ConnectStatusTextView);

        // toggle button for virtual flight control enable/disable
        mTogVirtualSticks.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) { // virtual sticks enabled
                if (ifFlightController() && !mVirtualStickControlState) {
                    mFlightController.setVirtualStickModeEnabled(true, djiError -> {
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
                if (ifFlightController() && mVirtualStickControlState) {
                    mFlightController.setVirtualStickModeEnabled(false, djiError -> {
                        if (djiError != null) {
                            // show error and flip back checked if failed
                            showToast(djiError.getDescription());
                            mTogVirtualSticks.setChecked(true);
                        }
                        else {
                            // show toast and updated control
                            mVirtualStickControlState = false;
                            showToast("Virtual Sticks Disabled");
                            killFlightManagementTasks();
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
                break;

            case R.id.btn_land:
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
                break;

            case R.id.btn_set_home:  // set home case
                if (ifFlightController()) {
                    mFlightController.setHomeLocationUsingAircraftCurrentLocation(
                        // nullable callback
                        djiError -> {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            } else {
                                showToast("Home Set!");
                                // get new home coordinates and update to textview
                                updateHomePos();
                            }
                        }
                    );
                }
                break;

            case R.id.btn_rth: // special return to home
                if (ifFlightController()) {
                    //TODO write this lmao
                }
                break;

            case R.id.btn_set_craft_flat:
                if (ifFlightController()) {
                    resetAircraftOrientation();
                }
                break;

            case R.id.btn_reload:
                    Button reload = ma.findViewById(R.id.btn_reload);
                    reload.setText("...");
                    updateTitleBar();
                    initFlightController();

                    Handler awaitReload = new Handler();
                    awaitReload.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reload.setText("reload");
                        }
                    }, 500);
                break;

            default:
                break;
        }
    }

    /*
     * API Control Methods VVVVV
     */
    /**
     * input float degree -180 to 180 to set pitch angle of craft
     */
    public void setPitch (float deg) {
        mPitch = deg;
        startFlightManagementTasks();
    }

    /**
     * input float degree -180 to 180 to set roll angle of craft
     */
    public void setRoll (float deg) {
        mRoll = deg;
        startFlightManagementTasks();
    }

    /**
     *  input float velocity for deg/second rotation of craft on yaw axis
     */
    public void setYaw (float vel) {
        mYaw = vel;
        startFlightManagementTasks();
    }

    /**
     * input float value for target velocity on z axis (positive results in craft ascend)
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
    public AircraftPositionalData getLocation () {
        return new AircraftPositionalData(mFlightControllerState.getAircraftLocation(),
                mFlightControllerState.getAttitude(), new LocationCoordinate2D(homeLat, homeLong), aircraftHomeHeading);
    }

    /**
     * set target future position value with LatLng class
     */
    public void setmTargetFuturePosition (LatLng target) {
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

    // class to hold all aircraft positional data, including home data
    class AircraftPositionalData {
        private final LocationCoordinate3D aircraftCurrentLocation;
        private final Attitude aircraftCurrentAttitude;
        private final LocationCoordinate2D aircraftHomeLocation;
        private final double aircraftHomeHeading;
        public final LatLng aircraftLatLng;
        public final LatLng homeLatLng;

        public AircraftPositionalData (LocationCoordinate3D aircraftCurrentLocationIN,
                                       Attitude aircraftCurrentAttitudeIN,
                                       LocationCoordinate2D aircraftHomeLocationIN,
                                       double homeHeadingIN) {
            // define class vars
            aircraftCurrentLocation = aircraftCurrentLocationIN;
            aircraftCurrentAttitude = aircraftCurrentAttitudeIN;
            aircraftHomeLocation = aircraftHomeLocationIN;
            aircraftHomeHeading = homeHeadingIN;

            aircraftLatLng = new LatLng(aircraftCurrentLocation.getLatitude(),
                    aircraftCurrentLocation.getLongitude());
            homeLatLng = new LatLng(aircraftHomeLocation.getLatitude(),
                    aircraftHomeLocation.getLongitude());
        }

        public double getAircraftLatitude () { return aircraftCurrentLocation.getLatitude(); }
        public double getAircraftLongitude () { return aircraftCurrentLocation.getLongitude(); }
        public float getAircraftAltitude () { return aircraftCurrentLocation.getAltitude(); }

        public double getAircraftPitch () { return aircraftCurrentAttitude.pitch; }
        public double getAircraftRoll () { return aircraftCurrentAttitude.roll; }

        /**
         * @return single positive value in degrees clockwise from true north
         */
        public double getAircraftYaw () {
            // make yaw single positive value clockwise from true north
            double yaw;
            if (aircraftCurrentAttitude.yaw < 0) {
                yaw = 360 - aircraftCurrentAttitude.yaw;
            }
            else {
                yaw = aircraftCurrentAttitude.yaw;
            }
            return yaw;
        }

        /**
         * @return double value of aircraft yaw from its home heading (pos/neg 180deg)
         */
        public double getAircraftHeadingRefHome() {
            return calcHeadingDifference(aircraftHomeHeading, getAircraftYaw());
        }

        public double getHomeLatitude () { return aircraftHomeLocation.getLatitude(); }
        public double getHomeLongitude () { return aircraftHomeLocation.getLongitude(); }
        public double getHomeHeading () { return aircraftHomeHeading;}

        /**
         * @return double value in meters of current position relative to home (from right of craft)
         */
        public XYValues getAircraftMeterOffsetFromHome() {
            return getXYOffsetBetweenPointsNormalToOriginHeading(
                    homeLatLng, aircraftHomeHeading, aircraftLatLng);
        }

    }

    /**
     * simple class to hold two x y offset values (used for positioning offsets)
     */
    class XYValues {

        public final double X;
        public final double Y;

        public XYValues (double xIN, double yIN) {
            X = xIN;
            Y = yIN;
        }
    }

    /**
     * @param baseRef the heading to reference second heading from
     * @param secRef the heading to calculate offset of from baseRef
     * @return double value of difference between two headings that are (originally) referenced to true north
     * return is within +/- 180 to have full range and denote quadrant
     */
    public double calcHeadingDifference(double baseRef, double secRef) {
        double finalOut = 0;

        finalOut = secRef - baseRef;

        // flip angle for finalOut if greater than 180 for clockwise angling issue
        if (finalOut > 180) {
            finalOut -= 360;
        }

        return finalOut;
    }

    /**
     * @return double value in meters between origin point and target point with header
     */
    public XYValues getXYOffsetBetweenPointsNormalToOriginHeading (LatLng origin, double originHeading, LatLng target) {
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
                mFlightController.sendVirtualStickFlightControlData(
                        new FlightControlData(mPitch, mRoll, mYaw, mThrottle),
                        djiError -> {}
                );
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
