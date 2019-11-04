package com.example.hairsalonbooking.Interface;

import com.example.hairsalonbooking.Model.Banner;

import java.util.List;

public interface IBannerLoadListener {
    void onBannerLoadSuccess(List<Banner> banners);
    void onLoadBannerFailed(String message);
}
