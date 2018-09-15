package com.example.shadesix.w2d.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DELL5547 on 06-Mar-18.
 */

public class ModelCurrentDeliveryDetails {

    @SerializedName("quantity")
    public String quantity;

    @SerializedName("return_cans")
    public String return_cans;

    @SerializedName("payment_mode")
    public String payment_mode;

    @SerializedName("delivery_address")
    public String delivery_address;

    @SerializedName("latitude")
    public Double latitude;

    @SerializedName("longitude")
    public Double longitude;

    @SerializedName("price")
    public String price;

    @SerializedName("order_status")
    public int order_status;

    @SerializedName("remarks")
    public String remarks;

    @SerializedName("user")
    public ModelUserDetails userDetails;


}
