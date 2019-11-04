package com.example.hairsalonbooking.Interface;

import com.example.hairsalonbooking.Model.Banner;

import java.util.List;

public interface ILookBookLoadListener {
    void onLookBookLoadSuccess(List<Banner> banners);
    void onLoadLookBookFailed(String message);
}
