package com.example.hairsalonbooking.Interface;

import com.example.hairsalonbooking.Model.BookingInfomation;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<BookingInfomation> timeSlotList);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotLoadEmpty();
}
