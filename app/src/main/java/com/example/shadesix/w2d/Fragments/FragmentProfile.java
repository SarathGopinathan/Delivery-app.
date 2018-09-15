package com.example.shadesix.w2d.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shadesix.w2d.LoginActivity;
import com.example.shadesix.w2d.Models.ModelProfile;
import com.example.shadesix.w2d.R;
import com.example.shadesix.w2d.Utils.CircleImagePicasso;
import com.example.shadesix.w2d.Utils.Constant;
import com.example.shadesix.w2d.Utils.Utilities;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;
import com.squareup.picasso.Picasso;

import cz.msebera.android.httpclient.Header;

/**
 * Created by shade six on 1/9/2018.
 */

public class FragmentProfile extends Fragment{
    boolean sentToSettings = false;
    ImageView dp;
    int timeout = 300000;
    String emergencyContact="";
    TextView name,userid,delivered_cans;
    Button emergency,logout;
    ProgressDialog  progressDialog;
    public FragmentProfile() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,container,false);
        dp = (ImageView) view.findViewById(R.id.dp);
        name = (TextView) view.findViewById(R.id.username);
        userid = (TextView) view.findViewById(R.id.userid);
        delivered_cans = (TextView) view.findViewById(R.id.total_del_cans);
        emergency = (Button) view.findViewById(R.id.emergency);
        logout = (Button) view.findViewById(R.id.log_out);
        progressDialog = new ProgressDialog(getContext());

        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        //method loads the users details
        getUserDetails();

        //logs out the user
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder logoutBuilder = new AlertDialog.Builder(getContext());
                logoutBuilder.setTitle("Logout");
                logoutBuilder.setCancelable(true);
                logoutBuilder.setMessage("Are you sure you want to logout?");

                logoutBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //clears shared prefs and starts login activity

                        Utilities.saveToUserDefault(getContext(), Constant.VEHICLE_ID,"");
                        Utilities.saveToUserDefault(getContext(),Constant.DRIVER_ID,"");
                        Utilities.saveToUserDefault(getContext(),Constant.AUTH_TOKEN,"");
                        Utilities.saveToUserDefault(getContext(),Constant.VEHICLE_CAPACITY,"");
                        Utilities.saveToUserDefault(getContext(),Constant.CURRENT_DELIV_ID,"");
                        Utilities.saveToUserDefault(getContext(),Constant.CURRENT_DELIV_STATUS,"");
                        dialogInterface.cancel();
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        getActivity().finish();
                    }
                });
                logoutBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // closes the dialog
                        dialogInterface.cancel();
                    }
                });
                logoutBuilder.show();
            }
        });

        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //gets call permission from user
                getCallPermission();
            }
        });
        return view;
    }

    private void getUserDetails() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("auth-token",Utilities.getFromeUserDefault(getActivity(),Constant.AUTH_TOKEN));
        client.setTimeout(timeout);
        client.get(Constant.BASE_URL+"driver/profile",new FragmentProfile.ResponceHandlerClass());

    }

    public void getCallPermission(){
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) getContext(), Manifest.permission.CALL_PHONE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Need Call Permission");
                builder.setMessage("W2D needs Call Permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.CALL_PHONE}, 100);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.CALL_PHONE}, 100);
            }
        } else {
            //You already have the permission, just go ahead.
            call();
        }
    }

    private void call() {

        // calls the given emergency number
        try {
            Intent phoneIntent = new Intent(Intent.ACTION_CALL);
            phoneIntent.setData(Uri.parse("tel:"+ emergencyContact));
            startActivity(phoneIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                call();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)getContext(), Manifest.permission.CALL_PHONE)) {
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Need Call Permission");
                    builder.setMessage("W2D needs Call Permission");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                            ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.CALL_PHONE}, 100);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(getActivity(),"Unable to get Permission",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class ResponceHandlerClass extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            ModelProfile model = new Gson().fromJson(new String(responseBody),ModelProfile.class);

            if(model.success == 1){

                //successfully loads all data
                try{
                    Picasso.with(getContext()).load(Constant.BASE_IMAGE_URL+model.driverDetails.license_image)
                            .transform(new CircleImagePicasso()).fit().centerCrop().into(dp);
                }catch (NullPointerException e){
                    Picasso.with(getContext()).load(R.drawable.default_dp)
                            .transform(new CircleImagePicasso()).fit().centerCrop().into(dp);
                }

                name.setText(model.driverDetails.driver_name);
                userid.setText("ID Number: "+model.driverDetails.id);
                delivered_cans.setText(model.total_cans_delivered);
                emergencyContact = model.emergency_contact;
                progressDialog.dismiss();
            }
            else{
                // auth key expired so clears the shared prefs and starts login activity
                if(model.error_code==404 || model.error_code==450){
                    Utilities.saveToUserDefault(getContext(),Constant.VEHICLE_ID,"");
                    Utilities.saveToUserDefault(getContext(),Constant.DRIVER_ID,"");
                    Utilities.saveToUserDefault(getContext(),Constant.AUTH_TOKEN,"");
                    Utilities.saveToUserDefault(getContext(),Constant.VEHICLE_CAPACITY,"");
                    Utilities.saveToUserDefault(getContext(),Constant.CURRENT_DELIV_ID,"");

                    startActivity(new Intent(getContext(), LoginActivity.class));
                    progressDialog.dismiss();
                    getActivity().finish();
                }
                else{
                    Toast.makeText(getContext(),"Something went wrong...",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(getContext(),"Seems like your network connectivity is down or very slow!",Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }
}
