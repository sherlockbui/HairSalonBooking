package com.example.hairsalonbooking.Interface;

import com.example.hairsalonbooking.Model.Salon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> salonList);
    void onBranchLoadFailed(String message);
}
