package com.example.hairsalonbooking.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Salon implements Parcelable {
    private String name, adress, website, phone, openHours, salonId;

    public Salon() {
    }

    protected Salon(Parcel in) {
        name = in.readString();
        adress = in.readString();
        website = in.readString();
        phone = in.readString();
        openHours = in.readString();
        salonId = in.readString();
    }

    public Salon(String name, String adress, String website, String phone, String openHours, String salonId) {
        this.name = name;
        this.adress = adress;
        this.website = website;
        this.phone = phone;
        this.openHours = openHours;
        this.salonId = salonId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(adress);
        dest.writeString(website);
        dest.writeString(phone);
        dest.writeString(openHours);
        dest.writeString(salonId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Salon> CREATOR = new Creator<Salon>() {
        @Override
        public Salon createFromParcel(Parcel in) {
            return new Salon(in);
        }

        @Override
        public Salon[] newArray(int size) {
            return new Salon[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOpenHours() {
        return openHours;
    }

    public void setOpenHours(String openHours) {
        this.openHours = openHours;
    }

    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }
}
