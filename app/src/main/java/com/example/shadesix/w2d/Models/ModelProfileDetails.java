package com.example.shadesix.w2d.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by shade six on 3/12/2018.
 */

public class ModelProfileDetails {

    @SerializedName("driver_name")
    public String driver_name;

    @SerializedName("license_image")
    public String license_image;

    @SerializedName("id")
    public String id;

    @SerializedName("dp")
    public String dp;

    @SerializedName("rating")
    public String rating;

    @SerializedName("review")
    public String review;

    @SerializedName("cans_delivered")
    public String cans_delivered;

}
