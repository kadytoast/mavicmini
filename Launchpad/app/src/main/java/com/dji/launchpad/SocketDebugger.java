package com.dji.launchpad;

import android.text.TextUtils;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * class for interacting with custom socketio based wireless debug client
 *
 * create class instance, then after app launch, take input of server ip and port, then connect
 * and start sending log info from method calls, use passed arguments
 */
public class SocketDebugger {

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
            mSocket = IO.socket("http://" + uri + ":" + port);
            mSocket.connect();
            return true;
        } catch (URISyntaxException e) {
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
    public boolean sendDebug(String message) {
        if (TextUtils.isEmpty(message) || mSocket == null) {
            return false;
        }
        String event = "debug";
        mSocket.emit(event, message);
        return true;
    }

}
