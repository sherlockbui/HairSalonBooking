package com.example.hairsalonbooking.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Barber implements Parcelable {
    private String id, name, username;
    private long rating;

    public Barber() {
    }

    public Barber(String id, String name, String username, long rating) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.rating = rating;
    }

    protected Barber(Parcel in) {
        id = in.readString();
        name = in.readString();
        username = in.readString();
        rating = in.readLong();
    }

    public static final Creator<Barber> CREATOR = new Creator<Barber>() {
        @Override
        public Barber createFromParcel(Parcel in) {
            return new Barber(in);
        }

        @Override
        public Barber[] newArray(int size) {
            return new Barber[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getRating() {
        return rating;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(username);
        dest.writeLong(rating);
    }
}
