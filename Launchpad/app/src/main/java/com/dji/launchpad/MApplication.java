package com.dji.launchpad;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;


public class MApplication extends Application {

    private AircraftObjHandler objHandler;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if (objHandler == null) {
            objHandler = new AircraftObjHandler();
            objHandler.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        objHandler.onCreate();
    }

}
