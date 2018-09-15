package com.example.shadesix.w2d.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DELL5547 on 28-Feb-18.
 */

public class ModelLogin {

    @SerializedName("error_code")
    public int error_code;

    @SerializedName("message")
    public String message;

    @SerializedName("success")
    public int success;

    @SerializedName("auth_token")
    public String auth_token;

    @SerializedName("vehicle_id")
    public String vehicle_id;

    @SerializedName("vehicle_capacity")
    public String vehicle_capacity;

    @SerializedName("driver_id")
    public String driver_id;

}
