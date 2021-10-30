package com.mavicmini.importsdk;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MApplication extends Application {

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        // install dji sdk, must be done before using any sdk stuff
        Helper.install(MApplication.this);
    }
}