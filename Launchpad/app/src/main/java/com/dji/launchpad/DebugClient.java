package com.dji.launchpad;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * class for interacting with custom socketio based wireless debug client
 *
 * create class instance, then after app launch, take input of server ip and port, then connect
 * and start sending log info from method calls, use passed arguments
 */
public class DebugClient {

    private Uri basepath = null;
    private RequestQueue queue;

    /**
     * sets up debugclient and instantiates volley request queue
     * @param context context from client code
     */
    public DebugClient (Context context) {
        queue = Volley.newRequestQueue(context);
    }

    /**
     * creates socket object with passed info and attempts to connect
     * @param ip ip address of target debug server
     * @param port port of listening debug server
     * @return boolean of creation and connection success
     */
    public boolean setPath(String ip, String port) {
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .encodedAuthority(ip + ":" + port)
                    .appendPath("debug");
            basepath = builder.build();
            return true;
        } catch (Error e) {
            System.out.println("createSocket caught : " + e.getMessage());
            basepath = null;
            return false;
        }
    }

    /**
     * sends passed text to set path in post body
     * @param message string to be sent back over http
     */
    public void log(String message) {
        if (TextUtils.isEmpty(message) || basepath == null) {
            System.out.println("false from debug log");
            return;
        }
        // exporting basepath to string url
        String url = basepath.toString();

        System.out.println(url);

        // building new request to add to volley queue
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //System.out.println("in response listener: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("in error listener: " + error.getMessage());
                System.out.println("in error listener: " + error.getCause());
            }
        })
        {
            // override getparams method of request to add post body
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> body = new HashMap<>();
                body.put("time", LocalDateTime.now().toString());
                body.put("msg",  message);
                return body;
            }
        };

        // add constructed request to volley queue
        queue.add(request);
        System.out.println("true from debug log");
    }

    /**
     * simpler method to handle error logging
     * calls internal log method with stack trace and message/cause
     * @param e exception to pass
     */
    public void errlog(Exception e) {
        // ensure cause information is non-null
        String cause = "";
        try {
            cause = Objects.requireNonNull(e.getCause()).getMessage();
        }
        catch (NullPointerException n) {
            cause = "no cause found";
        }
        // append sanitized information
        this.log(Arrays.toString(e.getStackTrace()) + "\n" +
                e.getMessage() + "\n" +
                cause);
    }

    /**
     * simpler method to handle error logging with extra message
     * @param e error to be passed
     * @param msg msg to be tagged with error (location etc)
     */
    public void errlog(Exception e, String msg) {
        // ensure cause information is non-null
        String cause = "";
        try {
            cause = Objects.requireNonNull(e.getCause()).getMessage();
        }
        catch (NullPointerException n) {
            cause = "no cause found";
        }

        // append sanitized information
        this.log("WITH " + msg + " : \n" +
                Arrays.toString(e.getStackTrace()) + "\n" +
                e.getMessage() + "\n" +
                cause);
    }

}
