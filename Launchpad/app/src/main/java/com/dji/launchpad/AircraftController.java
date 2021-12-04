package com.dji.launchpad;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    private Timer mSendVirtualStickDataTimer;
    private SendVirtualStickDataTask mSendVirtualStickDataTask;

    private float mPitch;
    private float mRoll;
    private float mYaw;
    private float mThrottle;

    private double homeLat;
    private double homeLong;
    private float homeAlt;

    private MainActivity ma;

    public AircraftController (MainActivity maIn) {
        ma = maIn;
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
        if (null != mSendVirtualStickDataTimer) {
            mSendVirtualStickDataTask.cancel();
            mSendVirtualStickDataTask = null;
            mSendVirtualStickDataTimer.cancel();
            mSendVirtualStickDataTimer.purge();
            mSendVirtualStickDataTimer = null;
        }
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
                homeAlt = mFlightControllerState.getTakeoffLocationAltitude();

                mTextViewHome.setText("Latitude : " + homeLat + "\nLongitude : " + homeLong + "\nAltitude: " +
                        homeAlt);
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

    // TODO implement movement methods with reference to original example for confirmation, then test

    private void initFlightController() {

        Aircraft aircraft = AircraftObjHandler.getAircraftInstance();
        if (aircraft == null || !aircraft.isConnected()) {
            showToast("Disconnected");
            mFlightController = null;
            mFlightControllerState = null;
        } else {
            mFlightController = aircraft.getFlightController();
            mFlightController.setRollPitchControlMode(RollPitchControlMode.ANGLE);
            mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
            mFlightController.setVerticalControlMode(VerticalControlMode.POSITION);
            mFlightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
            //TODO set max heights as needed for safety in environment

            mFlightController.setStateCallback(stateData -> {

                LocationCoordinate3D flightPos = stateData.getAircraftLocation();
                Attitude flightAtt = stateData.getAttitude();

                String yaw = String.format("%.2f", flightAtt.yaw);
                String pitch = String.format("%.2f", flightAtt.pitch);
                String roll = String.format("%.2f", flightAtt.roll);
                String positionX = String.format("%.2f", flightPos.getLatitude());
                String positionY = String.format("%.2f", flightPos.getLongitude());
                String positionZ = String.format("%.2f", flightPos.getAltitude());

                mTextViewPosition.setText("Yaw : " + yaw + ", Pitch : " + pitch + ", Roll : " + roll + "\n" + ", PosX : " + positionX +
                        ", PosY : " + positionY +
                        ", PosZ : " + positionZ);
            });
            mFlightControllerState = mFlightController.getState();

        }
    }

    private void initUI() {
        // sets up click listener for buttons and global textviews

        Button mBtnTakeOff = (Button) ma.findViewById(R.id.btn_take_off);
        Button mBtnLand = (Button) ma.findViewById(R.id.btn_land);
        mTextViewPosition = (TextView) ma.findViewById(R.id.textview_position);
        mTextViewHome = ma.findViewById(R.id.textview_homecoords);
        mConnectStatusTextView = (TextView) ma.findViewById(R.id.ConnectStatusTextView);
        Button mBtnHome = (Button) ma.findViewById(R.id.btn_set_home);
        //TODO add toggle button for virtual sticks with color/text change
        Button mBtnEnableControl = ma.findViewById(R.id.btn_enable_control);
        Button mBtnDisableControl = ma.findViewById(R.id.btn_disable_control);

        mBtnTakeOff.setOnClickListener(this);
        mBtnLand.setOnClickListener(this);
        mBtnHome.setOnClickListener(this);
        mBtnEnableControl.setOnClickListener(this);
        mBtnDisableControl.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_take_off:
                if (mFlightController != null){
                    mFlightController.startTakeoff(
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    if (djiError != null) {
                                        showToast(djiError.getDescription());
                                    } else {
                                        showToast("Take off Success");
                                    }
                                }
                            }
                    );
                }

                break;

            case R.id.btn_land:
                if (mFlightController != null){
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
                if (mFlightController != null) {
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

            case R.id.btn_enable_control:
                if (mFlightController != null) {
                    mFlightController.setVirtualStickModeEnabled(true, djiError -> {
                        if (djiError != null) {
                            showToast(djiError.getDescription());
                        }
                    });
                }

            case R.id.btn_disable_control:
                if (mFlightController != null) {
                    mFlightController.setVirtualStickModeEnabled(false, djiError -> {
                        if (djiError != null) {
                            showToast(djiError.getDescription());
                        }
                    });
                }

            default:
                break;
        }
    }

    class SendVirtualStickDataTask extends TimerTask {

        @Override
        public void run() {
            mTextViewPosition.setText("virtualstick ran");

            if (mFlightController != null) {
                mFlightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                mPitch, mRoll, mYaw, mThrottle
                        ), djiError -> {

                        }
                );
            }
        }
    }

}
