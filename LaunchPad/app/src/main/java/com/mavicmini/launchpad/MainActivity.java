package com.mavicmini.launchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

/** API 30 **/
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set dji perms
        checkAndRequestPermissions();

        // when done, confirm its prep
        TextView hellotxt = findViewById(R.id.textHello);
        hellotxt.setText(R.string.doneperms);
    }

    public void checkAndRequestPermissions () {
    }
}