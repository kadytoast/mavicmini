package com.dji.launchpad;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void OnCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_set_home);
    }

}
