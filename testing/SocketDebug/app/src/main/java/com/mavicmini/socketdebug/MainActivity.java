package com.mavicmini.socketdebug;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SocketDebug debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        debug = new SocketDebug();

        initUI();
    }

    private void initUI () {
        Button btnConnect = findViewById(R.id.btn_connect);
        Button btnSendDebug = findViewById(R.id.btn_senddebug);

        btnConnect.setOnClickListener(this);
        btnSendDebug.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                System.out.println("debugenter pressed");
                EditText entry = findViewById(R.id.editText_debugaddress);
                String address = entry.getText().toString();
                System.out.println("address content " + address);

                try {
                    if (!address.isEmpty()) {
                        // get and validate ip address
                        String ip = address.substring(0, address.indexOf(':'));
                        String zeroTo255
                                = "(\\d{1,2}|(0|1)\\"
                                + "d{2}|2[0-4]\\d|25[0-5])";
                        String regex
                                = zeroTo255 + "\\."
                                + zeroTo255 + "\\."
                                + zeroTo255 + "\\."
                                + zeroTo255;
                        boolean ifip = Pattern.matches(regex, ip);
                        System.out.println(ifip);

                        // get and validate port number
                        String port = address.substring(address.indexOf(':') + 1);
                        boolean ifport = (0 < Integer.parseInt(port) && Integer.parseInt(port) <= 65535);
                        System.out.println(ifport);

                        if (ifip && ifport) {
                            debug.createSocket(ip, port);
                            debug.log("Opened Debugger" + LocalDateTime.now());
                        }
                    }

                }
                catch (Error e){
                    System.out.println("debugenter button caught : " + e.getMessage());
                }
                break;

            case R.id.btn_senddebug:
                EditText msgenter = findViewById(R.id.editText_message);
                String message = msgenter.getText().toString();
                debug.log(message);
                break;
            default:
                break;
        }
    }
}