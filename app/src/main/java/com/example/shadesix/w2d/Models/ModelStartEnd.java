package com.example.shadesix.w2d.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by shadesix on 17-09-2018.
 */

public class ModelStartEnd {

    @SerializedName("error_code")
    public int error_code;

    @SerializedName("message")
    public String message;

    @SerializedName("success")
    public int success;

}
