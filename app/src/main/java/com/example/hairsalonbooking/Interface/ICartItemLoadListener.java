package com.example.hairsalonbooking.Interface;

import com.example.hairsalonbooking.Database.CartItem;

import java.util.List;

public interface ICartItemLoadListener {
    void onGetAllItemFromCartSuccess(List<CartItem> cartItemList);
}
