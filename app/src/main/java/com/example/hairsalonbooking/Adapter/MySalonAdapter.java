package com.example.hairsalonbooking.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Interface.IRecyclerItemSelectedListener;
import com.example.hairsalonbooking.Model.Salon;
import com.example.hairsalonbooking.R;

import java.util.ArrayList;
import java.util.List;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> {
    Context context;
    List<Salon> salonList;
    List<CardView> cardViewList;
    LocalBroadcastManager localBroadcastManager;


    public MySalonAdapter(Context context, List<Salon> salonList) {
        this.context = context;
        this.salonList = salonList;
        cardViewList = new ArrayList<>();
        localBroadcastManager = LocalBroadcastManager.getInstance(context);

        //Nếu chọn vị trí chưa có salon thì put Broadcast = null để disable button next
        if(salonList.size()<=0){
            Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
            intent.putExtra(Common.KEY_SALON_STORE, (boolean[]) null);
            localBroadcastManager.sendBroadcast(intent);
        }


    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_salon, parent, false);
        //Khi chuyển địa điểm => disable button next, chỉ enable khi đã chọn salon
        Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
        intent.putExtra(Common.KEY_SALON_STORE, (boolean[]) null);
        localBroadcastManager.sendBroadcast(intent);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.txt_salon_name.setText(salonList.get(position).getName());
        holder.txt_salon_adress.setText(salonList.get(position).getAdress());
        if (!cardViewList.contains(holder.card_salon))
            cardViewList.add(holder.card_salon);
        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int position) {
                for (CardView cardView : cardViewList)
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                holder.card_salon.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

                //APTER CHANGED COLOR FOR 2 BUTTON => tell BookingActivity know we already selected salon,
                // so BookingActitvity need enable button Next to process
                // next==> use LocalBroadcastListener.

                //sent Broadcast to tell Booking Activity enable button next.

                Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                intent.putExtra(Common.KEY_SALON_STORE, salonList.get(position));
                intent.putExtra(Common.KEY_STEP,1);
                localBroadcastManager.sendBroadcast(intent);


            }
        });


    }

    @Override
    public int getItemCount() {
        return salonList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_salon_name, txt_salon_adress;
        CardView card_salon;
        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_salon = itemView.findViewById(R.id.card_salon);
            txt_salon_adress = itemView.findViewById(R.id.txt_salon_adress);
            txt_salon_name = itemView.findViewById(R.id.txt_salon_name);
            itemView.setOnClickListener(this);

        }

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v, getAdapterPosition());
        }
    }
}
