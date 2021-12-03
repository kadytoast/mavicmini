package com.dji.launchpad;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;


public class MApplication extends Application {

    private AircraftObjHandler simulatorApplication;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if (simulatorApplication == null) {
            simulatorApplication = new AircraftObjHandler();
            simulatorApplication.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        simulatorApplication.onCreate();
    }

}
