package com.example.hairsalonbooking;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairsalonbooking.Adapter.AllBookingInfoAdapter;
import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Common.MySocket;
import com.example.hairsalonbooking.Common.SpaceItemDecoration;
import com.example.hairsalonbooking.Model.BookingInfomation;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AllBookingInfomation extends AppCompatActivity {
    RecyclerView recycler_allbooking_info;
    List<BookingInfomation> bookingInfomationList;
    AllBookingInfoAdapter adapter;
    private Socket mSocket = MySocket.getmSocket();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_booking_infomation);
        mSocket.connect();
        recycler_allbooking_info = findViewById(R.id.recycler_allbookinginfo);
        recycler_allbooking_info.setLayoutManager(new LinearLayoutManager(this));
        recycler_allbooking_info.addItemDecoration(new SpaceItemDecoration(8));
        recycler_allbooking_info.setHasFixedSize(true);
        bookingInfomationList = new ArrayList<>();
        adapter = new AllBookingInfoAdapter(this, bookingInfomationList);
        adapter.notifyDataSetChanged();
        recycler_allbooking_info.setAdapter(adapter);
        mSocket.emit("getAllBookingInfo", Common.currentUser.getPhoneNumber()).on("getAllBookingInfo", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject object = (JSONObject) args[0];
                BookingInfomation bookingInfomation = new BookingInfomation();
                if (object != null) {
                    try {
                        bookingInfomation.set_id(object.getString("_id"));
                        bookingInfomation.setCustomerName(object.getString("customerName"));
                        bookingInfomation.setCustomerPhone(object.getString("customerPhone"));
                        bookingInfomation.setDate(object.getString("date"));
                        bookingInfomation.setBarberId(object.getString("barberId"));
                        bookingInfomation.setBarberName(object.getString("barberName"));
                        bookingInfomation.setSalonId(object.getString("salonId"));
                        bookingInfomation.setSalonName(object.getString("salonName"));
                        bookingInfomation.setSalonAddress(object.getString("salonAddress"));
                        bookingInfomation.setSlot(object.getInt("slot"));
                        bookingInfomation.setDone(object.getBoolean("done"));
                        bookingInfomation.setTime(new StringBuilder(Common.convertTimeSlotToString(Integer.parseInt(object.getString("slot"))))
                                .append(" at ")
                                .append(object.getString("date")).toString());
                        bookingInfomationList.add(bookingInfomation);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }
}
