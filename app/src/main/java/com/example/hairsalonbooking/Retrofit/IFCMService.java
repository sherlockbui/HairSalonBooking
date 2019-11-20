package com.example.hairsalonbooking.Retrofit;

import com.example.hairsalonbooking.Model.FCMResponse;
import com.example.hairsalonbooking.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAYqruUJ8:APA91bE7aRD0Paz6NHU8meM0tX2J9oXzpoO57eFemGwe6yeguS6GtXK1Bm5CgBrK-3_uJHdWJjDcuQE1MsqCM27y4_HMXh0vftsr16rldy-mkQ6krIP2RsE78-WkRHbxoWWnQQYTcQug"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
