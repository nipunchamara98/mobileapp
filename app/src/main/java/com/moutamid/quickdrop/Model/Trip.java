package com.moutamid.quickdrop.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Trip implements Parcelable{

    private String id;
    private String userId;
    private String pickup;
    private String dropoff;
    private String price;
    private int time;
    private String riderId;
    private String cashStatus;
    private int distance;
    private String status;
    private String disability;

    public Trip(){

    }


    public Trip(String id, String userId, String pickup, String dropoff, String price, int time,
                String riderId, String cashStatus,String status,int distance,String disability) {
        this.id = id;
        this.userId = userId;
        this.pickup = pickup;
        this.dropoff = dropoff;
        this.price = price;
        this.time = time;
        this.riderId = riderId;
        this.cashStatus = cashStatus;
        this.status = status;
        this.distance = distance;
        this.disability = disability;
    }

    protected Trip(Parcel in) {
        id = in.readString();
        userId = in.readString();
        pickup = in.readString();
        dropoff = in.readString();
        price = in.readString();
        time = in.readInt();
        riderId = in.readString();
        cashStatus = in.readString();
        distance = in.readInt();
        status = in.readString();
        disability = in.readString();
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCashStatus() {
        return cashStatus;
    }

    public void setCashStatus(String cashStatus) {
        this.cashStatus = cashStatus;
    }

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getDropoff() {
        return dropoff;
    }

    public void setDropoff(String dropoff) {
        this.dropoff = dropoff;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getDisability() {
        return disability;
    }

    public void setDisability(String disability) {
        this.disability = disability;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(userId);
        parcel.writeString(pickup);
        parcel.writeString(dropoff);
        parcel.writeString(price);
        parcel.writeInt(time);
        parcel.writeString(riderId);
        parcel.writeString(cashStatus);
        parcel.writeInt(distance);
        parcel.writeString(status);
        parcel.writeString(disability);
    }
}
