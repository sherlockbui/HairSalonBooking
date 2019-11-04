package com.example.hairsalonbooking;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.hairsalonbooking.Adapter.MyViewPagerAdapter;
import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Common.MySocket;
import com.example.hairsalonbooking.Common.NonSwipeViewPager;
import com.example.hairsalonbooking.Model.Barber;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.shuhart.stepview.StepView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class BookingActivity extends AppCompatActivity {

    LocalBroadcastManager localBroadcastManager;
    AlertDialog dialog;
    StepView stepView;
    NonSwipeViewPager viewPager;
    Button btn_previous_step, btn_next_step;
    ArrayList<Barber> barberList;
    Socket mSocket= MySocket.getmSocket();
    private BroadcastReceiver buttonNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int step = intent.getIntExtra(Common.KEY_STEP,0);
            if(step==1){
                Common.currentSalon = intent.getParcelableExtra(Common.KEY_SALON_STORE);
            }else if(step==2){
                Common.currentBarber = intent.getParcelableExtra(Common.KEY_BARBER_SELECTED);

            }else if(step==3){
                Common.currentTimeSlot = intent.getIntExtra(Common.KEY_TIME_SLOT, -1);

            }
            if(Common.currentSalon!=null){
                btn_next_step.setEnabled(true);
            }else {
                btn_next_step.setEnabled(false);
            }

            setColorButton();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(buttonNextReceiver, new IntentFilter(Common.KEY_ENABLE_BUTTON_NEXT));
        initView();
        initControl();
        setUpStepView();
        setColorButton();

    }

    private void initControl() {
        btn_next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.step < 3 || Common.step == 0) {
                    Common.step++;
                    if (Common.step == 1) {
                        if (Common.currentSalon != null) {
                            barberList = new ArrayList<>();
                            mSocket.emit("getBarbers", Common.currentSalon.getSalonId());
                            loadBarberBySalon(barberList);
                            barberList= new ArrayList<>();
                        }

                    }
                    else if(Common.step==2){
                        if(Common.currentBarber != null){
                            loadTimeSlotOfBarber(Common.currentBarber.getId());
                        }
                    }
                    else if(Common.step==3){
                        if(Common.currentTimeSlot != -1){
                            confirmBooking();
                        }
                    }
                    viewPager.setCurrentItem(Common.step);
                }
            }
        });
        btn_previous_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.step == 3 || Common.step > 0) {
                    Common.step--;
                    viewPager.setCurrentItem(Common.step);
                    if(Common.step<3){
                        btn_next_step.setEnabled(true);
                        setColorButton();
                    }
                }
            }
        });
    }

    private void confirmBooking() {
        Intent intent = new Intent(Common.KEY_CONFIRM_BOOKING);
        localBroadcastManager.sendBroadcast(intent);

    }

    private void loadTimeSlotOfBarber(String barberId) {
        Intent intent = new Intent(Common.KEY_DISLAY_TIME_SLOT);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void loadBarberBySalon(final ArrayList<Barber> barberArrayList) {
        final Intent intent = new Intent(Common.KEY_BARBER_LOAD_DONE);
        Emitter.Listener onGetBarber = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject object = (JSONObject) args[0];
                        try {
                            barberArrayList.add(new Barber(
                                    object.getString("_id"),
                                    object.getString("name"),
                                    object.getString("username"),
                                    object.getLong("rating")));
                            intent.putParcelableArrayListExtra(Common.KEY_BARBER_LOAD_DONE,barberArrayList);
                            localBroadcastManager.sendBroadcast(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        mSocket.on("getBarbers", onGetBarber);


    }

    private void setColorButton() {
        if (btn_next_step.isEnabled()) {
            btn_next_step.setBackgroundResource(R.color.colorButton);
        } else {
            btn_next_step.setBackgroundResource(android.R.color.darker_gray);
        }
        if (btn_previous_step.isEnabled()) {
            btn_previous_step.setBackgroundResource(R.color.colorButton);
        } else {
            btn_previous_step.setBackgroundResource(android.R.color.darker_gray);
        }
    }

    private void setUpStepView() {
        List<String> stepList = new ArrayList<>();
        stepList.add("Salon");
        stepList.add("Baber");
        stepList.add("Time");
        stepList.add("Confirm");
        stepView.setSteps(stepList);
    }

    private void initView() {
        mSocket.connect();
        dialog = new SpotsDialog.Builder().setContext(this).build();
        stepView = findViewById(R.id.step_view);
        viewPager = findViewById(R.id.view_pager);
        btn_next_step = findViewById(R.id.btn_next_step);
        btn_previous_step = findViewById(R.id.btn_previous_step);
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                stepView.go(position, true);
                if (position == 0) {
                    btn_previous_step.setEnabled(false);

                } else {
                    btn_previous_step.setEnabled(true);
                }
                if (position == 3) {
                    btn_next_step.setEnabled(false);

                } else {
                    btn_next_step.setEnabled(false);
                }

                setColorButton();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        localBroadcastManager.unregisterReceiver(buttonNextReceiver);
        super.onDestroy();
    }
}
