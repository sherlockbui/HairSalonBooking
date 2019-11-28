package com.example.hairsalonbooking.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Model.BookingInfomation;
import com.example.hairsalonbooking.R;

import java.util.List;

public class MyHistoryAdapter extends RecyclerView.Adapter<MyHistoryAdapter.MyViewHolder> {
    Context context;
    List<BookingInfomation> bookingInfomationList;

    public MyHistoryAdapter(Context context, List<BookingInfomation> bookingInfomationList) {
        this.context = context;
        this.bookingInfomationList = bookingInfomationList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_history, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_booking_date.setText(bookingInfomationList.get(position).getDate());
        holder.txt_booking_barber_text.setText(bookingInfomationList.get(position).getBarberName());
        holder.txt_salon_address.setText(bookingInfomationList.get(position).getSalonAddress());
        holder.txt_salon_name.setText(bookingInfomationList.get(position).getSalonName());
        holder.txt_booking_time_text.setText(new StringBuilder("Giờ: ").append(Common.convertTimeSlotToString(bookingInfomationList.get(position).getSlot())));
    }

    @Override
    public int getItemCount() {
        return bookingInfomationList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txt_salon_name, txt_salon_address, txt_booking_time_text, txt_booking_barber_text, txt_booking_date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_booking_date = itemView.findViewById(R.id.txt_booking_date);
            txt_salon_name = itemView.findViewById(R.id.txt_salon_name);
            txt_salon_address = itemView.findViewById(R.id.txt_salon_address);
            txt_booking_time_text = itemView.findViewById(R.id.txt_booking_time_text);
            txt_booking_barber_text = itemView.findViewById(R.id.txt_booking_barber_text);
        }
    }
}
