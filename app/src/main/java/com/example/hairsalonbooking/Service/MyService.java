package com.example.hairsalonbooking.Service;

import android.content.Intent;
import android.util.Log;

import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Common.MySocket;
import com.example.hairsalonbooking.Model.MyToken;
import com.example.hairsalonbooking.RatingBarberActivity;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Random;

public class MyService extends FirebaseMessagingService {
    private final String ID_BARBER = "ID_BARBER";
    Socket mSocket = MySocket.getmSocket();
    private FirebaseAuth mAuth;


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            MyToken myToken = new MyToken();
            myToken.setToken(token);
            myToken.setPhoneCustomer("0" + currentUser.getPhoneNumber().substring(3));
            String jsonToken = new Gson().toJson(myToken);
            mSocket.emit("updateToken", jsonToken);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Intent intent = new Intent(this, RatingBarberActivity.class);
        intent.putExtra(ID_BARBER, remoteMessage.getData().get(Common.ID_BARBER_KEY));
        Log.d("token", "onMessageReceived: " + remoteMessage);
        Common.showNotification(this, new Random().nextInt(),
                remoteMessage.getData().get(Common.TITLE_KEY),
                remoteMessage.getData().get(Common.CONTENT_KEY), intent);
    }
}
