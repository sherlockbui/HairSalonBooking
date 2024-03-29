package com.example.hairsalonbooking.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairsalonbooking.Adapter.MyTimeSlotAdapter;
import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Common.MySocket;
import com.example.hairsalonbooking.Common.SpaceItemDecoration;
import com.example.hairsalonbooking.Interface.ITimeSlotLoadListener;
import com.example.hairsalonbooking.Model.BookingInfomation;
import com.example.hairsalonbooking.Model.MyToken;
import com.example.hairsalonbooking.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class BookingStep3Fragment extends Fragment implements ITimeSlotLoadListener {

    static BookingStep3Fragment instance;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    LocalBroadcastManager localBroadcastManager;
    Socket mSocket= MySocket.getmSocket();
    RecyclerView recycler_time_slot;
    HorizontalCalendarView calendarView;
    SimpleDateFormat simpleDateFormat;
    List<BookingInfomation> timeSlotList;


    BroadcastReceiver dislayTimeSlot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, 0);
            loadAvailableTimeSlotOfBarber(Common.currentBarber
                    .getId(), simpleDateFormat.format(date.getTime()));
            //get token by barber
            mSocket.emit("getToken", Common.currentBarber.getId()).on("getToken", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject object = (JSONObject) args[0];
                    if (object != null) {
                        MyToken token = new MyToken();
                        try {
                            token.setIdBarber(object.getString("idbarber"));
                            token.setToken(object.getString("token"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Common.currentToken = token;
                        Log.d("TOKEN", "call: " + Common.currentToken.getToken());
                    } else {
                        Common.currentToken = null;
                    }
                }
            });

        }
    };

    public static BookingStep3Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep3Fragment();
        return instance;

    }

    private void loadAvailableTimeSlotOfBarber(String id, String time) {
        timeSlotList = new ArrayList<>();
        mSocket.emit("getTimeBooking", id, time);
        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlotList);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSocket.connect();
        iTimeSlotLoadListener = this;
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(dislayTimeSlot, new IntentFilter(Common.KEY_DISLAY_TIME_SLOT));
        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");

    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(dislayTimeSlot);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_booking_step3, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {

        calendarView = view.findViewById(R.id.calendar_view);
        recycler_time_slot = view.findViewById(R.id.recycler_time_slot);
        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpaceItemDecoration(8));


        //Calendar
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 2);//2 day left

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar
                .Builder(view, R.id.calendar_view)
                .range(startDate, endDate).datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .build();
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date;
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getId()
                            , simpleDateFormat.format(date.getTime()));
                }
            }
        });

    }
    @Override
    public void onTimeSlotLoadSuccess(final List<BookingInfomation> timeSlotList) {
        final MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext(), timeSlotList);
        recycler_time_slot.setAdapter(adapter);
        Emitter.Listener getTimeBooking = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Handler mHandler = new Handler(Looper.getMainLooper());
                    JSONObject object = (JSONObject) args[0];
                        try {
                            if (object == null) {
                                iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                            } else {
                                BookingInfomation bookingInfomation = new BookingInfomation();
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
                                timeSlotList.add(bookingInfomation);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


            }
        };
        mSocket.on("getTimeBooking", getTimeBooking);
    }
    @Override
    public void onTimeSlotLoadFailed(String message) {
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext());
        recycler_time_slot.setAdapter(adapter);

    }

}
