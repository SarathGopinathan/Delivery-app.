package com.example.shadesix.w2d.Models;

import com.google.gson.annotations.SerializedName;

public class ModelDispatched {

    @SerializedName("message")
    public String message;

    @SerializedName("success")
    public int success;

    @SerializedName("error_code")
    public int error_code;

}
