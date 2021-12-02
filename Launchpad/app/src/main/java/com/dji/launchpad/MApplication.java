package com.dji.launchpad;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;


public class MApplication extends Application {
    // attaches base context to use with dji sdk and loads necessary classes with <Helper>
    private AircraftHandler simulatorApplication;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if (simulatorApplication == null) {
            simulatorApplication = new AircraftHandler();
            simulatorApplication.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        simulatorApplication.onCreate();
    }

}