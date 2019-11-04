package com.example.hairsalonbooking.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Database.CartDatabase;
import com.example.hairsalonbooking.Database.CartItem;
import com.example.hairsalonbooking.Database.DatabaseUtils;
import com.example.hairsalonbooking.Interface.IRecyclerItemSelectedListener;
import com.example.hairsalonbooking.Model.ShoppingItem;
import com.example.hairsalonbooking.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyShoppingItemAdapter extends RecyclerView.Adapter<MyShoppingItemAdapter.MyViewHolder> {
    Context context;
    List<ShoppingItem> shoppingItems;
    CartDatabase cartDatabase;

    public MyShoppingItemAdapter(Context context, List<ShoppingItem> shoppingItems) {
        this.context = context;
        this.shoppingItems = shoppingItems;
        cartDatabase = CartDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_shopping_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(shoppingItems.get(position).getImage()).into(holder.img_shopping_item);
        holder.txt_shopping_item_name.setText(Common.formatShoppingItemName(shoppingItems.get(position).getName()));
        holder.txt_shopping_price.setText(new StringBuilder("$").append(shoppingItems.get(position).getPrice()));

        //Add to cart
        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int position) {
                //Create cart item
                CartItem cartItem = new CartItem();
                cartItem.setProductId(shoppingItems.get(position).getId());
                cartItem.setProductName(shoppingItems.get(position).getName());
                cartItem.setProductImage(shoppingItems.get(position).getImage());
                cartItem.setProductQuantity(1);
                cartItem.setProductPrice(Long.valueOf(shoppingItems.get(position).getPrice()));
                cartItem.setUserPhone(Common.currentUser.getPhoneNumber());

                //Insert to Db.
                DatabaseUtils.insertToCart(cartDatabase,cartItem);
                Toast.makeText(context, "Added to Cart !!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return shoppingItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_shopping_item_name, txt_shopping_price, txt_add_to_card;
        ImageView img_shopping_item;


        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;


        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img_shopping_item = itemView.findViewById(R.id.img_shopping_item);
            txt_shopping_item_name = itemView.findViewById(R.id.txt_name_shoppong_item);
            txt_shopping_price = itemView.findViewById(R.id.txt_price_shopping_item);
            txt_add_to_card = itemView.findViewById(R.id.txt_add_to_card);

            txt_add_to_card.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v,getAdapterPosition());
        }
    }
}
