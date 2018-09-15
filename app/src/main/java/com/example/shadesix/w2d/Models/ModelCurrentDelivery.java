package com.example.shadesix.w2d.Models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by DELL5547 on 06-Mar-18.
 */

public class ModelCurrentDelivery {

    @SerializedName("error_code")
    public int error_code;

    @SerializedName("message")
    public String message;

    @SerializedName("success")
    public int success;

    @SerializedName("order")
    public ArrayList<ModelCurrentDeliveryDetails>order_details;


}
