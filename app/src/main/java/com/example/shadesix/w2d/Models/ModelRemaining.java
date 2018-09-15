package com.example.shadesix.w2d.Models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by shade six on 1/9/2018.
 */

public class ModelRemaining {

    @SerializedName("message")
    public String message;

    @SerializedName("success")
    public int success;

    @SerializedName("error_code")
    public int error_code;

    @SerializedName("orders")
    public ArrayList<ModelRemainingDetails> ordersList;
}
