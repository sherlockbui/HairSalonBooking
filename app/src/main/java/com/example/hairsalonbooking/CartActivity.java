package com.example.hairsalonbooking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;

import android.widget.TextView;

import com.example.hairsalonbooking.Adapter.MyCartAdapter;
import com.example.hairsalonbooking.Database.CartDatabase;
import com.example.hairsalonbooking.Database.CartItem;
import com.example.hairsalonbooking.Database.DatabaseUtils;
import com.example.hairsalonbooking.Interface.ICartItemLoadListener;
import com.example.hairsalonbooking.Interface.ICartItemUpdateListener;
import com.example.hairsalonbooking.Interface.ISumCartListener;

import java.util.List;

public class CartActivity extends AppCompatActivity implements ICartItemLoadListener, ICartItemUpdateListener, ISumCartListener {
    RecyclerView recycler_cart;
    TextView txt_total_price;
    Button btn_submit_cart;
    CartDatabase cartDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        initView();
    }

    private void initView() {
        txt_total_price = findViewById(R.id.txt_total_price);
        btn_submit_cart = findViewById(R.id.btn_submit_cart);
        cartDatabase = CartDatabase.getInstance(this);
        DatabaseUtils.getAllCart(cartDatabase, this);
        recycler_cart = findViewById(R.id.recycler_cart);
        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager  linearLayoutManager = new LinearLayoutManager(this);
        recycler_cart.setLayoutManager(linearLayoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(this,linearLayoutManager.getOrientation()));

    }

    @Override
    public void onGetAllItemFromCartSuccess(List<CartItem> cartItemList) {

        MyCartAdapter adapter = new MyCartAdapter(this, cartItemList,this);
        recycler_cart.setAdapter(adapter);

    }

    @Override
    public void onCartItemUpdateSuccess() {
        DatabaseUtils.sumCart(cartDatabase,this);
    }

    @Override
    public void onSumCartSuccess(Long value) {
        txt_total_price.setText(new StringBuilder("$").append(value));
    }
}
