package com.example.hairsalonbooking.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairsalonbooking.Adapter.MySalonAdapter;
import com.example.hairsalonbooking.Common.MySocket;
import com.example.hairsalonbooking.Common.SpaceItemDecoration;
import com.example.hairsalonbooking.Interface.IAllSalonListener;
import com.example.hairsalonbooking.Interface.IBranchLoadListener;
import com.example.hairsalonbooking.Model.Salon;
import com.example.hairsalonbooking.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;


public class BookingStep1Fragment extends Fragment implements IAllSalonListener, IBranchLoadListener {

    static BookingStep1Fragment instance;
    IAllSalonListener iAllSalonListener;
    IBranchLoadListener iBranchLoadListener;
    AlertDialog dialog;
    MaterialSpinner spinner;
    RecyclerView recycler_salon;
    List<String> list_salon;
    List<Salon> branchList;
    private Socket mSocket= MySocket.getmSocket();

    public static BookingStep1Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep1Fragment();
        return instance;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSocket.connect();
        dialog = new SpotsDialog.Builder().setContext(getContext()).build();
        iAllSalonListener = this;
        iBranchLoadListener = this;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_booking_step1, container, false);
        initView(view);
        loadAllSalon(list_salon);
        return view;

    }

    private void initView(View view) {
        list_salon = new ArrayList<>();
        branchList = new ArrayList<>();
        spinner = view.findViewById(R.id.spinner);
        recycler_salon = view.findViewById(R.id.recycler_salon);
        recycler_salon.setHasFixedSize(true);
        recycler_salon.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recycler_salon.addItemDecoration(new SpaceItemDecoration(4));
        list_salon.add("Chọn Địa điểm ^^");
        mSocket.emit("getAllSalon", "");


    }

    private void loadAllSalon(List<String> list_salon) {
        iAllSalonListener.onAllSalonLoadSuccess(list_salon);

    }

    @Override
    public void onAllSalonLoadSuccess(final List<String> areaNameList) {
        mSocket.on("getAllSalon", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject object = (JSONObject) args[0];
                        try {
                            String salons = object.getString("name");
                            areaNameList.add(salons);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        spinner.setItems(areaNameList);
                    }
                });
            }
        });
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, final Object item) {
                if (position > 0) {
                    mSocket.emit("getBranch", item.toString());
                    loadBranchOfCity(branchList);
                    branchList = new ArrayList<>();
                }
            }

        });

    }


    private void loadBranchOfCity(final List<Salon> branchs) {
        iBranchLoadListener.onBranchLoadSuccess(branchs);
    }

    @Override
    public void onAllSalonLoadFailed(String message) {
        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBranchLoadSuccess(final List<Salon> branchs) {
        final MySalonAdapter adapter = new MySalonAdapter(getActivity(), branchs);
        recycler_salon.setAdapter(adapter);
        Emitter.Listener getBranch = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    JSONObject object = (JSONObject) args[0];

                    @Override
                    public void run() {
                        try {
                            branchs.add(new Salon(object.getString("name"), object.getString("adress"), object.getString("website"), object.getString("phone"), object.getString("openHours"), object.getString("_id")));
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        mSocket.on("getBranch", getBranch);
    }

    @Override
    public void onBranchLoadFailed(String message) {
        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show();
    }
}
