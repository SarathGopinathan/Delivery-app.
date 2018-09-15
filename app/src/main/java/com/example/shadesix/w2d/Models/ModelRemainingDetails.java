package com.example.shadesix.w2d.Models;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DELL5547 on 28-Feb-18.
 */

public class ModelRemainingDetails implements Comparable {

    @SerializedName("distance")
    public float distance;

    @SerializedName("id")
    public String id;

    @SerializedName("delivery_address")
    public String delivery_address;

    @SerializedName("quantity")
    public String quantity;

    @SerializedName("latitude")
    public String latitude;

    @SerializedName("longitude")
    public String longitude;

    @SerializedName("user")
    public ModelUserDetails userModel;


    public float getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Object o) {
        int comparedistance= (int) ((ModelRemainingDetails)o).getDistance();
        return (int)this.distance-comparedistance;
    }
}
