package com.example.hairsalonbooking.Fragments;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Common.MySocket;
import com.example.hairsalonbooking.Database.CartDatabase;
import com.example.hairsalonbooking.Database.CartItem;
import com.example.hairsalonbooking.Database.DatabaseUtils;
import com.example.hairsalonbooking.Interface.ICartItemLoadListener;
import com.example.hairsalonbooking.Model.BookingInfomation;
import com.example.hairsalonbooking.Model.FCMResponse;
import com.example.hairsalonbooking.Model.FCMSendData;
import com.example.hairsalonbooking.Model.MyNotification;
import com.example.hairsalonbooking.R;
import com.example.hairsalonbooking.Retrofit.IFCMService;
import com.example.hairsalonbooking.Retrofit.RetrofitClient;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BookingStep4Fragment extends Fragment implements ICartItemLoadListener {

    static BookingStep4Fragment instance;
    private static int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 9898;
    SimpleDateFormat simpleDateFormat;
    LocalBroadcastManager localBroadcastManager;
    TextView txt_booking_barber_text, txt_booking_time_text, txt_salon_address, txt_salon_name, txt_salon_open_hours, txt_salon_phone, txt_salon_website;
    Button btn_confirm;
    IFCMService ifcmService;
    Socket mSocket = MySocket.getmSocket();
    ICartItemLoadListener iCartItemLoadListener;


    BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setData();
        }
    };

    public static BookingStep4Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep4Fragment();
        return instance;

    }

    private void setData() {
        txt_booking_barber_text.setText(Common.currentBarber.getName());
        txt_booking_time_text.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(Common.bookingDate.getTime())));
        txt_salon_address.setText(Common.currentSalon.getAdress());
        txt_salon_website.setText(Common.currentSalon.getWebsite());
        txt_salon_name.setText(Common.currentSalon.getName());
        txt_salon_open_hours.setText(Common.currentSalon.getOpenHours());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSocket.connect();
        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);
        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_booking_step4, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        txt_booking_barber_text = view.findViewById(R.id.txt_booking_barber_text);
        txt_booking_time_text = view.findViewById(R.id.txt_booking_time_text);
        txt_salon_address = view.findViewById(R.id.txt_salon_address);
        txt_salon_name = view.findViewById(R.id.txt_salon_name);
        txt_salon_open_hours = view.findViewById(R.id.txt_salon_open_hour);
        txt_salon_phone = view.findViewById(R.id.txt_salon_phone);
        txt_salon_website = view.findViewById(R.id.txt_salon_website);
        btn_confirm = view.findViewById(R.id.btn_confirm);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmBooking();

            }
        });
    }

    private void confirmBooking() {
        DatabaseUtils.getAllCart(CartDatabase.getInstance(getContext()), this);

    }

    private void addToCalendar(Calendar bookingDate, String startDate) {
        String[] convertTime = startDate.split("-");
        String[] startTimeCovert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeCovert[0].trim());
        int startMinInt = Integer.parseInt(startTimeCovert[1].trim());

        String[] endTimeCovert = convertTime[1].split(":");
        int endHourInt = Integer.parseInt(endTimeCovert[0].trim());
        int endMinInt = Integer.parseInt(endTimeCovert[1].trim());

        //Start event Calendar
        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY, startHourInt);
        startEvent.set(Calendar.MINUTE, startMinInt);
        //End event Calendar
        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY, endHourInt);
        endEvent.set(Calendar.MINUTE, endMinInt);

        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd_MM_yyyy HH:mm");
        String startEventTime = calendarDateFormat.format(startEvent.getTime());
        String endEventTime = calendarDateFormat.format(endEvent.getTime());

        addToDeviceCalendar(startEventTime, endEventTime, "Haircut Booking",
                new StringBuilder("Haircut from ")
                        .append(startDate)
                        .append(" with ")
                        .append(Common.bookingInfomation.getBarberName())
                        .append(" at ")
                        .append(Common.bookingInfomation.getSalonName()).toString(), new StringBuilder("Address: ").append(Common.bookingInfomation.getSalonAddress()).toString());

    }

    private void addToDeviceCalendar(String startEventTime, String endEventTime, String title, String description, String location) {
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd_MM_yyyy HH:mm");

        try {
            Date start = calendarDateFormat.parse(startEventTime);
            Date end = calendarDateFormat.parse(endEventTime);
            ContentValues event = new ContentValues();

            //Put
            event.put(CalendarContract.Events.CALENDAR_ID, getCalendar(getContext()));
            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.DESCRIPTION, description);
            event.put(CalendarContract.Events.EVENT_LOCATION, location);

            //Time
            event.put(CalendarContract.Events.DTSTART, start.getTime());
            event.put(CalendarContract.Events.DTEND, end.getTime());
            event.put(CalendarContract.Events.ALL_DAY, 0);
            event.put(CalendarContract.Events.HAS_ALARM, 1);

            String timeZone = TimeZone.getDefault().getID();
            event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);

            Uri calendars;
            if (Build.VERSION.SDK_INT >= 8) {
                calendars = Uri.parse("content://com.android.calendar/events");
            } else {
                calendars = Uri.parse("content://calendar/events");
            }
            getActivity().getContentResolver().insert(calendars, event);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getCalendar(Context context) {
        String gmailIdCalendar = "";
        String[] projection = {"_id", "calendar_displayName"};

        Uri calendars;
        if (Build.VERSION.SDK_INT >= 8) {
            calendars = Uri.parse("content://com.android.calendar/calendars");
        } else {
            calendars = Uri.parse("content://calendar/events");
        }
        ContentResolver contentResolver = context.getContentResolver();
        //Select all calendar
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);
        if (managedCursor.moveToFirst()) {
            String calName;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do {
                calName = managedCursor.getString(nameCol);
                if (calName.contains("@gmail.com") || calName.contains("@fpt.edu.vn")) {
                    gmailIdCalendar = managedCursor.getString(idCol);
                    break;
                } else {
                    gmailIdCalendar = "0";
                }
            } while (managedCursor.moveToNext());
            managedCursor.close();
        }

        return gmailIdCalendar;
    }


    private void resetData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentSalon = null;
        Common.currentBarber = null;
        Common.bookingDate.add(Calendar.DATE,0);
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        super.onDestroy();

    }

    @Override
    public void onGetAllItemFromCartSuccess(List<CartItem> cartItemList) {

        final BookingInfomation bookingInfomation = new BookingInfomation();
        final String barberId = Common.currentBarber.getId();
        final String barberName = Common.currentBarber.getName();
        final String customerName = Common.currentUser.getFullName();
        final String customerPhone = Common.currentUser.getPhoneNumber().trim();
        final String salonId = Common.currentSalon.getSalonId();
        final String salonAddress = Common.currentSalon.getAdress();
        final String salonName = Common.currentSalon.getName();
        final String date = simpleDateFormat.format(Common.bookingDate.getTime());
        final int slot = Common.currentTimeSlot;

        bookingInfomation.setBarberId(barberId);
        bookingInfomation.setBarberName(barberName);
        bookingInfomation.setCustomerName(customerName);
        bookingInfomation.setCustomerPhone(customerPhone);
        bookingInfomation.setSalonId(salonId);
        bookingInfomation.setSalonAddress(salonAddress);
        bookingInfomation.setSalonName(salonName);
        bookingInfomation.setDone(false);
        bookingInfomation.setTime(new StringBuilder(Common.convertTimeSlotToString(slot))
                .append(" at ")
                .append(simpleDateFormat.format(Common.bookingDate.getTime())).toString());
        bookingInfomation.setSlot(Common.currentTimeSlot);
        MyNotification myNotification = new MyNotification();
        myNotification.setIdBarber(barberId);
        myNotification.setTitle("New Booking");
        myNotification.setContent("You have a new appoiment from phone number " + Common.currentUser.getPhoneNumber());
        myNotification.setRead(false);
        final String notification = new Gson().toJson(myNotification);
        final String cartItemsToJson = new Gson().toJson(cartItemList);
        Log.d("AAAAA", "onGetAllItemFromCartSuccess: " + cartItemsToJson);
        if (Common.currentToken != null) {
            FCMSendData sendRequest = new FCMSendData();
            Map<String, String> dataSend = new HashMap<>();
            dataSend.put(Common.TITLE_KEY, "New Booking");
            dataSend.put(Common.CONTENT_KEY, "You have new booking from user" + Common.currentUser.getFullName());
            sendRequest.setTo(Common.currentToken.getToken());
            Log.d("token", "onClick: " + Common.currentToken.getToken());
            sendRequest.setData(dataSend);
            ifcmService.sendNotification(sendRequest).subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe(new Consumer<FCMResponse>() {
                @Override
                public void accept(FCMResponse fcmResponse) throws Exception {
                    mSocket.emit("addBooking", barberId, barberName, customerName, customerPhone, salonId, salonAddress, salonName, slot, false, date, notification, cartItemsToJson)
                            .on("addBooking", new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    JSONObject object = (JSONObject) args[0];
                                    if (object != null) {
                                        Common.bookingInfomation = bookingInfomation;
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                addToCalendar(Common.bookingDate, Common.convertTimeSlotToString(slot));
                                                DatabaseUtils.clearCart(CartDatabase.getInstance(getContext()));
                                                resetData();
                                                Toast.makeText(getActivity(), "Thành Công", Toast.LENGTH_SHORT).show();
                                                getActivity().finish();
                                            }
                                        });
                                    } else {
                                        resetData();
                                        Toast.makeText(getContext(), "Lỗi", Toast.LENGTH_SHORT).show();
                                        getActivity().finish();
                                    }
                                }
                            });
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    Log.d("NOTIFICATION_ERROR", "notification_error: " + throwable.getMessage());
                }
            });
        }

    }
}
