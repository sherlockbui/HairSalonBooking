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
import com.example.hairsalonbooking.Model.BookingInfomation;
import com.example.hairsalonbooking.Model.MyNotification;
import com.example.hairsalonbooking.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class BookingStep4Fragment extends Fragment {

    static BookingStep4Fragment instance;
    Socket mSocket= MySocket.getmSocket();
    SimpleDateFormat simpleDateFormat;
    LocalBroadcastManager localBroadcastManager;
    TextView txt_booking_barber_text, txt_booking_time_text, txt_salon_address, txt_salon_name, txt_salon_open_hours, txt_salon_phone, txt_salon_website;
    Button btn_confirm;

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
                final BookingInfomation bookingInfomation = new BookingInfomation();
                final String barberId = Common.currentBarber.getId();
                String barberName = Common.currentBarber.getName();
                String customerName = Common.currentUser.getFullName();
                String customerPhone = Common.currentUser.getPhoneNumber().trim();
                String salonId = Common.currentSalon.getSalonId();
                String salonAddress = Common.currentSalon.getAdress();
                String salonName = Common.currentSalon.getName();
                final String date = simpleDateFormat.format(Common.bookingDate.getTime());
                final String slot = String.valueOf(Common.currentTimeSlot);

                bookingInfomation.setBarberId(barberId);
                bookingInfomation.setBarberName(barberName);
                bookingInfomation.setCustomerName(customerName);
                bookingInfomation.setCustomerPhone(customerPhone);
                bookingInfomation.setSalonId(salonId);
                bookingInfomation.setSalonAddress(salonAddress);
                bookingInfomation.setSalonName(salonName);
                bookingInfomation.setDone(false);
                bookingInfomation.setTime(new StringBuilder(slot)
                        .append(" at ")
                        .append(simpleDateFormat.format(Common.bookingDate.getTime())).toString());
                bookingInfomation.setSlot(String.valueOf(Common.currentTimeSlot));
                MyNotification myNotification = new MyNotification();
                myNotification.setIdBarber(barberId);
                myNotification.setTitle("New Booking");
                myNotification.setContent("You have a new appoiment from phone "+ Common.currentUser.getPhoneNumber());
                myNotification.setRead(false);
                String notification = new Gson().toJson(myNotification);
                mSocket.emit("addBooking", barberId, barberName, customerName, customerPhone, salonId, salonAddress, salonName, slot, false, date, notification);
                mSocket.on("addBooking", new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        final JSONObject object = (JSONObject) args[0];
                            getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(object!=null){
                                    Common.bookingInfomation = bookingInfomation;
//                                    addToCalendar(Common.bookingDate,Common.convertTimeSlotToString(Common.currentTimeSlot));
                                    resetData();// fix bug to continue booking (reset Common.step)
                                    Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
                                } else {
                                    Toast.makeText(getContext(), "Sorry, 1 phonenumber/1 slot per day", Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void addToCalendar(Calendar bookingDate, String startDate) {
        String[] convertTime = startDate.split("-"); //split 9:00 - 9:30 to 2 object timestart, timeend
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

        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String startEventTime = calendarDateFormat.format(startEvent.getTime());
        String endEventTime = calendarDateFormat.format(endEvent.getTime());

        addToDeviceCalendar(startEventTime, endEventTime, "Haircut Booking",
                new StringBuilder("Haircut from ")
                        .append(startDate)
                        .append(" with ")
                        .append(Common.currentBarber.getName())
                        .append(" at ")
                        .append(Common.currentSalon.getName()).toString(), new StringBuilder("Address: ").append(Common.currentSalon.getAdress()).toString());

    }

    private void addToDeviceCalendar(String startEventTime, String endEventTime, String title, String description, String location) {
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

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
                calendars = Uri.parse("content://com.android.calendar/calendars");
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
                if (calName.contains("@gmail.com")) {
                    gmailIdCalendar = managedCursor.getString(idCol);
                    break;
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
}
