package com.example.hairsalonbooking.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairsalonbooking.Adapter.MyBarberAdapter;
import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Common.SpaceItemDecoration;
import com.example.hairsalonbooking.Model.Barber;
import com.example.hairsalonbooking.R;

import java.util.ArrayList;

public class BookingStep2Fragment extends Fragment {
    static BookingStep2Fragment instance;
    RecyclerView recycler_barber;
    LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver barberDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Barber> barberArrayList = intent.getParcelableArrayListExtra(Common.KEY_BARBER_LOAD_DONE);
            MyBarberAdapter adapter = new MyBarberAdapter(getContext(), barberArrayList);
            recycler_barber.setAdapter(adapter);
        }
    };

    public static BookingStep2Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep2Fragment();
        return instance;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(barberDoneReceiver, new IntentFilter(Common.KEY_BARBER_LOAD_DONE));
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(barberDoneReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_booking_step2, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        recycler_barber = view.findViewById(R.id.recycler_barber);
        recycler_barber.setHasFixedSize(true);
        recycler_barber.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recycler_barber.addItemDecoration(new SpaceItemDecoration(4));

    }
}
