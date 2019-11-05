package com.example.hairsalonbooking.Common;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class MySocket {
    private static Socket mSocket;
    public static Socket getmSocket() {
        try {
            mSocket = IO.socket("http://10.0.2.2:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return mSocket.connect();
    }
}

