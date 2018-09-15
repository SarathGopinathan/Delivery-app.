package com.example.shadesix.w2d.Models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by shade six on 1/10/2018.
 */

public class ModelDelivered {

    @SerializedName("message")
    public String message;

    @SerializedName("success")
    public int success;

    @SerializedName("error_code")
    public int error_code;

    @SerializedName("order_details")
    public ArrayList<ModelDeliveredDetails> order_details;
}
