package com.example.hairsalonbooking.Adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Common.MySocket;
import com.example.hairsalonbooking.Model.BookingInfomation;
import com.example.hairsalonbooking.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.util.List;

public class AllBookingInfoAdapter extends RecyclerView.Adapter<AllBookingInfoAdapter.MyViewHolder> {
    Socket mSocket = MySocket.getmSocket();
    List<BookingInfomation> infomationList;
    Context context;

    public AllBookingInfoAdapter(Context context, List<BookingInfomation> infomationList) {
        mSocket.connect();
        this.infomationList = infomationList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_booking_info_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.namebarber.setText(infomationList.get(position).getBarberName());
        holder.address.setText(infomationList.get(position).getSalonAddress());
        holder.time.setText(infomationList.get(position).getTime());
        holder.img_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.emit("deleteBookingInfo", infomationList.get(position).get_id(), Common.currentUser.getPhoneNumber()).once("deleteBookingInfo", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        JSONObject object = (JSONObject) args[0];
                        if (object != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    infomationList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Thành Công", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Thất Bại", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return infomationList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView address, time, namebarber;
        ImageButton img_del;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.txt_salon_address);
            time = itemView.findViewById(R.id.txt_time);
            namebarber = itemView.findViewById(R.id.txt_salon_baber);
            img_del = itemView.findViewById(R.id.btn_del);
        }
    }
}
