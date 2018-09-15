package com.example.shadesix.w2d.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by shade six on 3/14/2018.
 */

public class ModelOrderCancelled {
    @SerializedName("message")
    public String message;

    @SerializedName("success")
    public int success;

    @SerializedName("error_code")
    public int error_code;
}
