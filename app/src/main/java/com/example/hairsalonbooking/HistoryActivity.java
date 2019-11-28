package com.example.hairsalonbooking;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairsalonbooking.Adapter.MyHistoryAdapter;
import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Common.MySocket;
import com.example.hairsalonbooking.Model.BookingInfomation;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class HistoryActivity extends AppCompatActivity {
    RecyclerView recycler_history;
    List<BookingInfomation> infomationList;
    private Socket mSocket = MySocket.getmSocket();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mSocket.connect();
        initView();
        loadUserBookingHistory();

    }

    private void loadUserBookingHistory() {
        infomationList = new ArrayList<>();
        mSocket.emit("getBookingHistory", Common.currentUser.getPhoneNumber());
    }

    private void initView() {
        final AlertDialog alertDialog = new SpotsDialog.Builder().setContext(this).build();

        recycler_history = findViewById(R.id.recycler_history);
        recycler_history.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_history.setLayoutManager(linearLayoutManager);
        recycler_history.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        mSocket.on("getBookingHistory", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONObject object = (JSONObject) args[0];
                final BookingInfomation bookingInfomation = new BookingInfomation();
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
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            infomationList.add(bookingInfomation);
                            MyHistoryAdapter adapter = new MyHistoryAdapter(getApplicationContext(), infomationList);
                            recycler_history.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
