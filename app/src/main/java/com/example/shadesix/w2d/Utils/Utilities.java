package com.example.shadesix.w2d.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by DELL5547 on 28-Feb-18.
 */

public class Utilities {

    public static void saveToUserDefault(Context context, String key,String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constant.SHARED_PREF,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }
    public static String getFromeUserDefault(Context context , String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constant.SHARED_PREF,Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }
}
