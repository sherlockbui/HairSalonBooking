package com.example.hairsalonbooking.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairsalonbooking.Adapter.HomeSliderAdapter;
import com.example.hairsalonbooking.Adapter.LookbookAdapter;
import com.example.hairsalonbooking.BookingActivity;
import com.example.hairsalonbooking.CartActivity;
import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Common.MySocket;
import com.example.hairsalonbooking.Database.CartDatabase;
import com.example.hairsalonbooking.Database.DatabaseUtils;
import com.example.hairsalonbooking.HistoryActivity;
import com.example.hairsalonbooking.Interface.IBannerLoadListener;
import com.example.hairsalonbooking.Interface.ICountItemCartListener;
import com.example.hairsalonbooking.Interface.ILookBookLoadListener;
import com.example.hairsalonbooking.Model.Banner;
import com.example.hairsalonbooking.Model.BookingInfomation;
import com.example.hairsalonbooking.R;
import com.example.hairsalonbooking.Service.PicassoService;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.nex3z.notificationbadge.NotificationBadge;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ss.com.bannerslider.Slider;


public class HomeFragment extends Fragment implements ILookBookLoadListener, IBannerLoadListener, ICountItemCartListener {
    LinearLayout layout_user_infomation;
    Slider banner_slider;
    RecyclerView recycler_look_book;
    TextView txt_user_name, txt_salon_address, txt_salon_barber, txt_time, txt_time_remain;
    IBannerLoadListener iBannerLoadListener;
    ILookBookLoadListener iLookBookLoadListener;
    List<Banner> banners;
    List<Banner> bannerslookbook;
    CardView card_view_booking, card_view_cart, card_booking_info, card_view_notification, card_view_history;
    NotificationBadge notificationBadge;
    CartDatabase cartDatabase;
    private Socket mSocket= MySocket.getmSocket();

    private Emitter.Listener getBookInfomation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            JSONObject object = (JSONObject) args[0];
            if (object != null) {
                Common.bookingInfomation = new BookingInfomation();
                    try {

                        Common.bookingInfomation.set_id(object.getString("_id"));
                        Common.bookingInfomation.setCustomerName(object.getString("customerName"));
                        Common.bookingInfomation.setCustomerPhone(object.getString("customerPhone"));
                        Common.bookingInfomation.setDate(object.getString("date"));
                        Common.bookingInfomation.setBarberId(object.getString("barberId"));
                        Common.bookingInfomation.setBarberName(object.getString("barberName"));
                        Common.bookingInfomation.setSalonId(object.getString("salonId"));
                        Common.bookingInfomation.setSalonName(object.getString("salonName"));
                        Common.bookingInfomation.setSalonAddress(object.getString("salonAddress"));
                        Common.bookingInfomation.setSlot(object.getInt("slot"));
                        Common.bookingInfomation.setDone(object.getBoolean("done"));
                        Common.bookingInfomation.setTime(new StringBuilder(Common.convertTimeSlotToString(Integer.parseInt(object.getString("slot"))))
                                .append(" at ")
                                .append(object.getString("date")).toString());
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (Common.bookingInfomation == null) {
                                    card_booking_info.setVisibility(View.GONE);
                                } else {
                                    card_booking_info.setVisibility(View.VISIBLE);
                                    txt_salon_address.setText(Common.bookingInfomation.getSalonAddress());
                                    txt_salon_barber.setText(Common.bookingInfomation.getBarberName());
                                    txt_time.setText(Common.bookingInfomation.getTime());
                                    Log.d("TEST", "curent: " + Calendar.getInstance().getTimeInMillis());
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        card_booking_info.setVisibility(View.GONE);
                    }
                });

            }

        }
    };
    private Emitter.Listener getBanners = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            Handler mHandler = new Handler(Looper.getMainLooper());
            JSONObject object = (JSONObject) args[0];
            try {
                final String image = object.getString("image");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        banners.add(new Banner(image));
                        loadBanner();
                        loadLookBook();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private Emitter.Listener getLookBook = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject object = (JSONObject) args[0];
            Handler mHandler = new Handler(Looper.getMainLooper());
            try {
                if (object != null) {
                    final String image = object.getString("image");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            bannerslookbook.add(new Banner(image));
                        }
                    });

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }



        }
    };

    public HomeFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iBannerLoadListener = this;
        iLookBookLoadListener = this;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mSocket.connect();
        mSocket.on("getLookBook", getLookBook);
        mSocket.on("getBanners", getBanners);
        mSocket.on("getBookInfomation", getBookInfomation);
        mSocket.emit("getBanners", "");
        mSocket.emit("getLookBook", "");
        initView(view);
        initControl();
        if (Common.currentUser != null) {
            setUserInfomation();
            countCartItem();
        }

        return view;
    }

    private void countCartItem() {
        DatabaseUtils.countItemInCart(cartDatabase, this);
    }

    private void initControl() {
        card_view_booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BookingActivity.class));

            }
        });
        card_view_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CartActivity.class));
            }
        });
        card_view_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HistoryActivity.class));
            }
        });
    }

    private void loadLookBook() {
        iLookBookLoadListener.onLookBookLoadSuccess(bannerslookbook);

    }

    private void loadBanner() {
        iBannerLoadListener.onBannerLoadSuccess(banners);


    }
    private void initView(View view) {
        cartDatabase = CartDatabase.getInstance(getContext());
        banners = new ArrayList<>();
        bannerslookbook = new ArrayList<>();
        layout_user_infomation = view.findViewById(R.id.layout_user_infomation);
        txt_user_name = view.findViewById(R.id.txt_user_name);
        banner_slider = view.findViewById(R.id.banner_slider);
        recycler_look_book = view.findViewById(R.id.recycler_look_book);
        card_view_booking = view.findViewById(R.id.card_view_booking);
        card_view_notification = view.findViewById(R.id.card_view_notification);
        card_view_history = view.findViewById(R.id.card_view_history);
        card_view_cart = view.findViewById(R.id.card_view_cart);
        Slider.init(new PicassoService());
        notificationBadge = view.findViewById(R.id.notification_badge);
        // Init Infomation Booking layout
        card_booking_info = view.findViewById(R.id.card_booking_info);
        txt_salon_address = view.findViewById(R.id.txt_salon_address);
        txt_salon_barber = view.findViewById(R.id.txt_salon_baber);
        txt_time = view.findViewById(R.id.txt_time);
        txt_time_remain = view.findViewById(R.id.txt_time_remain);
    }

    private void setUserInfomation() {
        layout_user_infomation.setVisibility(View.VISIBLE);
        txt_user_name.setText(Common.currentUser.getFullName());
    }

    @Override
    public void onLookBookLoadSuccess(List<Banner> banners) {
        recycler_look_book.setHasFixedSize(true);
        recycler_look_book.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_look_book.setAdapter(new LookbookAdapter(getActivity(), bannerslookbook));


    }

    @Override
    public void onLoadLookBookFailed(String message) {
        Toast.makeText(getActivity(), "Load LookBook Failed", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBannerLoadSuccess(List<Banner> banners) {
        banner_slider.setAdapter(new HomeSliderAdapter(banners));

    }

    @Override
    public void onLoadBannerFailed(String message) {
        Toast.makeText(getActivity(), "Load Banner Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCartItemCountSuccess(int count) {
        notificationBadge.setText(String.valueOf(count));
    }

    @Override
    public void onResume() {
        countCartItem();
        loadUserBooking();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void loadUserBooking() {
        mSocket.emit("getBookInfomation", null, Common.currentUser.getPhoneNumber());
    }


}
