package com.example.shadesix.w2d.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DELL5547 on 07-Mar-18.
 */

public class ModelDeliveredDetails {

    @SerializedName("first_name")
    public String first_name;

    @SerializedName("delivery_address")
    public String delivery_address;

    @SerializedName("last_name")
    public String last_name;

    @SerializedName("order_status")
    public int order_status;

    @SerializedName("price")
    public String price;

    @SerializedName("returned_cans")
    public String returned_cans;

    @SerializedName("payment_mode")
    public String payment_mode;
}
