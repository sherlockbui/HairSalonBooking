package com.example.hairsalonbooking.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairsalonbooking.Database.CartDatabase;
import com.example.hairsalonbooking.Database.CartItem;
import com.example.hairsalonbooking.Database.DatabaseUtils;
import com.example.hairsalonbooking.Interface.ICartItemUpdateListener;
import com.example.hairsalonbooking.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyViewHolder> {
    Context context;
    List<CartItem> cartItemList;
    CartDatabase cartDatabase;
    ICartItemUpdateListener iCartItemUpdateListener;

    public MyCartAdapter(Context context, List<CartItem> cartItemList, ICartItemUpdateListener iCartItemUpdateListener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.iCartItemUpdateListener = iCartItemUpdateListener;
        this.cartDatabase = CartDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_cart_item,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        Picasso.get().load(cartItemList.get(position).getProductImage()).into(holder.img_product);
        holder.txt_cart_name.setText(new StringBuilder(cartItemList.get(position).getProductName()));
        holder.txt_cart_price.setText(new StringBuilder("$").append(cartItemList.get(position).getProductPrice()));
        holder.txt_cart_quantity.setText(new StringBuilder(String.valueOf(cartItemList.get(position).getProductQuantity())));


        holder.setListener(new IImageButtonListener() {
            @Override
            public void onImageButtonClick(View view, int pos, boolean isDecrease) {
                if(isDecrease){
                    if(cartItemList.get(pos).getProductQuantity() > 0){
                        cartItemList.get(pos).setProductQuantity(cartItemList.get(pos).getProductQuantity()-1);
                        DatabaseUtils.updateCart(cartDatabase,cartItemList.get(pos));
                    }

                }
                else {
                    if(cartItemList.get(pos).getProductQuantity() < 99){
                        cartItemList.get(pos).setProductQuantity(cartItemList.get(pos).getProductQuantity()+1);
                        DatabaseUtils.updateCart(cartDatabase,cartItemList.get(pos));
                    }

                }
                holder.txt_cart_quantity.setText(new StringBuilder(String.valueOf(cartItemList.get(pos).getProductQuantity())));
                iCartItemUpdateListener.onCartItemUpdateSuccess();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    interface IImageButtonListener{
        void onImageButtonClick(View view, int pos, boolean isDecrease);
    }
    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView txt_cart_name, txt_cart_price, txt_cart_quantity;
        ImageView img_decrease, img_increase, img_product;
        IImageButtonListener listener;


        public void setListener(IImageButtonListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_cart_name = itemView.findViewById(R.id.txt_cart_name);
            txt_cart_price = itemView.findViewById(R.id.txt_cart_price);
            txt_cart_quantity = itemView.findViewById(R.id.txt_cart_quantity);
            img_decrease = itemView.findViewById(R.id.img_decrease);
            img_increase = itemView.findViewById(R.id.img_increase);
            img_product = itemView.findViewById(R.id.cart_img);


            img_decrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onImageButtonClick(v, getAdapterPosition(),true);

                }
            });

            img_increase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onImageButtonClick(v, getAdapterPosition(),false);

                }
            });
        }
    }
}
