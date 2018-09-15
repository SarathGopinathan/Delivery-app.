package com.example.shadesix.w2d.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by shade six on 3/12/2018.
 */

public class ModelProfile {

    @SerializedName("error_code")
    public int error_code;

    @SerializedName("message")
    public String message;

    @SerializedName("success")
    public int success;

    @SerializedName("total_cans_delivered")
    public String total_cans_delivered;

    @SerializedName("driver")
    public ModelProfileDetails driverDetails;

    @SerializedName("emergency_contact")
    public String emergency_contact;

}
