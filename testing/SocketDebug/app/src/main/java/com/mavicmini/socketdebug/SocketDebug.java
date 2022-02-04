package com.mavicmini.socketdebug;

import android.text.TextUtils;

import java.net.URI;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * class for interacting with custom socketio based wireless debug client
 *
 * create class instance, then after app launch, take input of server ip and port, then connect
 * and start sending log info from method calls, use passed arguments
 */
public class SocketDebug {

    private Socket mSocket = null;

    /**
     * creates socket object with passed info and attempts to connect
     * @param uri ip address of target debug server
     * @param port port of listening debug server
     * @return boolean of creation and connection success
     */
    public boolean createSocket(String uri, String port) {
        closeSocket();
        try {
            //
            System.out.println(uri);
            System.out.println(port);
            mSocket = IO.socket(URI.create("http://" + uri + ":" + port));
            mSocket.connect();
            System.out.println("socket created");
            // testing fucking shoot me
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println(mSocket.connected()); // true
                }
            });

            mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("socket failed to connect");
                }
            });

            return true;
        } catch (Error e) {
            System.out.println("createSocket caught : " + e.getMessage());
            mSocket = null;
            return false;
        }
    }

    /**
     * closes socket and sets back to null if socket is not already null
     */
    public void closeSocket() {
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }

    /**
     * sends passed text with event "debug"
     * @param message string to be sent back over ws
     * @return boolean if sending succeeds or if message is valid (false if empty or mSocket not setup)
     */
    public boolean log(String message) {
        if (TextUtils.isEmpty(message) || mSocket == null) {
            System.out.println("false from debug log");
            return false;
        }
        String event = "debug";
        mSocket.emit(event, message);
        System.out.println("true from debug log");
        return true;
    }

}
