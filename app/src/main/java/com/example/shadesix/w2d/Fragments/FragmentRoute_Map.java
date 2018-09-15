package com.example.shadesix.w2d.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shadesix.w2d.LoginActivity;
import com.example.shadesix.w2d.Models.ModelCurrentDelivery;
import com.example.shadesix.w2d.Models.ModelDistanceMatrix;
import com.example.shadesix.w2d.Models.ModelOrderCancelled;
import com.example.shadesix.w2d.Models.ModelOrderCompleted;
import com.example.shadesix.w2d.R;
import com.example.shadesix.w2d.Utils.Constant;
import com.example.shadesix.w2d.Utils.DirectionsParser;
import com.example.shadesix.w2d.Utils.GPSTracker;
import com.example.shadesix.w2d.Utils.Utilities;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

import static com.example.shadesix.w2d.LandingActivity.viewPager;

/**
 * Created by shade six on 1/9/2018.
 */

public class FragmentRoute_Map extends Fragment implements OnMapReadyCallback {
    DatabaseReference root;
    private static final int locationRequestCode = 500;
    LocationManager locationManager;
    Button cancel, completed;
    int temp=0,flag=0;
    int timeout = 300000;
    SlidingUpPanelLayout slidingUpPanelLayout;
    ImageView call;
    String reason = "",returnQuant="", userAddress = "", userName = "";
    ProgressDialog progressDialog,completedProgressDialog;
    public GoogleMap mMap;
    TextView quantity, cansReturn, name, address, phone,amount;
    Double latitude = 0.0, longitude = 0.0;
    String TAG = "Fragment RouteMap";
    String number = "";
    int paymentMode = 0;
    int finalPaid = 0;
    Location currentLocation;
    public LatLng destination,latLng;
    String distance = "", duration = "";
    public SupportMapFragment mSupportMapFragment;
    RelativeLayout rlNoDetails, rlContent, rlContentDetails,rlCanDetails,rlmapfragNodetails,rlDetails;

    public FragmentRoute_Map() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routemap, container, false);

        //initializing all views

        root = FirebaseDatabase.getInstance().getReference().child(Utilities.getFromeUserDefault(getContext(),Constant.VEHICLE_ID));
        progressDialog = new ProgressDialog(getContext());
        completedProgressDialog = new ProgressDialog(getContext());
        slidingUpPanelLayout = (SlidingUpPanelLayout) view.findViewById(R.id.slide_up_panel);
        rlContent = (RelativeLayout) view.findViewById(R.id.rl_content);
        rlDetails = (RelativeLayout) view.findViewById(R.id.rl_details);
        rlmapfragNodetails = (RelativeLayout) view.findViewById(R.id.rlmapfrag_nodetails);
        rlContentDetails = (RelativeLayout) view.findViewById(R.id.rl_content_details);
        rlNoDetails = (RelativeLayout) view.findViewById(R.id.rl_nodetails);
        rlCanDetails = (RelativeLayout) view.findViewById(R.id.rl_can_details);
        call = (ImageView) view.findViewById(R.id.icon_phone);
        cancel = (Button) view.findViewById(R.id.cancel);
        completed = (Button) view.findViewById(R.id.completed);
        quantity = (TextView) view.findViewById(R.id.delivery_cans);
        cansReturn = (TextView) view.findViewById(R.id.return_cans);
        name = (TextView) view.findViewById(R.id.name);
        amount = (TextView) view.findViewById(R.id.amount);
        address = (TextView) view.findViewById(R.id.address);
        phone = (TextView) view.findViewById(R.id.phone);
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mSupportMapFragment.getMapAsync(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    progressDialog.show();
                    //assign flag to 1 inorder to stop sorting the list again
                    if(flag == 0) {
                        flag = 1;
                    }
                    //if the id is not empty,show routemap and slideuppanel
                    if (!Utilities.getFromeUserDefault(getContext(), Constant.CURRENT_DELIV_ID).isEmpty()) {
                        loadDetails();
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        rlContentDetails.setVisibility(View.VISIBLE);
                        rlContent.setVisibility(View.VISIBLE);
                        rlCanDetails.setVisibility(View.VISIBLE);
                        rlDetails.setVisibility(View.VISIBLE);
                        rlNoDetails.setVisibility(View.GONE);
                    }
                    //if there is no id then show empty list icon and set map and slideuppanel visibility as gone
                    else {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        rlContent.setVisibility(View.GONE);
                        rlCanDetails.setVisibility(View.GONE);
                        rlDetails.setVisibility(View.GONE);
                        rlmapfragNodetails.setVisibility(View.VISIBLE);
                        rlContentDetails.setLayoutParams(new SlidingUpPanelLayout.LayoutParams(SlidingUpPanelLayout.LayoutParams.MATCH_PARENT,
                                SlidingUpPanelLayout.LayoutParams.MATCH_PARENT));
                        rlNoDetails.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //calls the current customers phone number
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calluser();
            }
        });

        // gets data and calls completedAPI
        completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.completed_dialog);
                dialog.show();

                int paid = 0;
                TextView add = (TextView) dialog.findViewById(R.id.btn_plus);
                TextView reduce = (TextView) dialog.findViewById(R.id.btn_minus);
                TextView  returnQuantity= (TextView) dialog.findViewById(R.id.txt_return_quantity);
                CheckBox paymentRecieved = (CheckBox) dialog.findViewById(R.id.checkbox_received_payment);
                RelativeLayout relativeLayoutCheckbox = (RelativeLayout) dialog.findViewById(R.id.rl_checkbox);
                Button complete = (Button) dialog.findViewById(R.id.complete);
                paymentRecieved.setChecked(true);

                returnQuantity.setText(returnQuant);

                if(Integer.parseInt(returnQuantity.getText().toString())==Integer.parseInt(returnQuant)){
                    add.setBackgroundColor(Color.parseColor("#606060"));
                    add.setClickable(false);
                }

                if(Integer.parseInt(returnQuantity.getText().toString())<=0){
                    reduce.setBackgroundColor(Color.parseColor("#606060"));
                    reduce.setClickable(false);
                }
                //checks max quant and doesnt let it add more than max count
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Integer.parseInt(returnQuantity.getText().toString())==Integer.parseInt(returnQuant)){
                            add.setBackgroundColor(Color.parseColor("#606060"));
                            add.setClickable(false);
                        }
                        else{
                            String tempQuant = String.valueOf(Integer.parseInt(returnQuantity.getText().toString())+1);
                            returnQuantity.setText(tempQuant);
                            if (Integer.parseInt(tempQuant)>=Integer.parseInt(returnQuant)){
                                add.setBackgroundColor(Color.parseColor("#606060"));
                                add.setClickable(false);
                            }
                            else {
                                add.setBackgroundColor(Color.parseColor("#3F51B5"));
                                add.setClickable(true);
                            }
                            if (tempQuant.equals("0")){
                                reduce.setBackgroundColor(Color.parseColor("#606060"));
                                reduce.setClickable(false);
                            }
                            else {
                                reduce.setBackgroundColor(Color.parseColor("#3F51B5"));
                                reduce.setClickable(true);
                            }
                        }
                    }
                });
                //checks quant and doesnt let it rewduce under 0
                reduce.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Integer.parseInt(returnQuantity.getText().toString())<=0){
                            reduce.setBackgroundColor(Color.parseColor("#606060"));
                            reduce.setClickable(false);
                        }
                        else{
                            String tempQuant = String.valueOf(Integer.parseInt(returnQuantity.getText().toString())-1);
                            returnQuantity.setText(tempQuant);
                            if (tempQuant.equals("0")){
                                reduce.setBackgroundColor(Color.parseColor("#606060"));
                                reduce.setClickable(false);
                            }
                            else {
                                reduce.setBackgroundColor(Color.parseColor("#3F51B5"));
                                reduce.setClickable(true);
                            }
                            if (Integer.parseInt(tempQuant)>=Integer.parseInt(returnQuant)){
                                add.setBackgroundColor(Color.parseColor("#606060"));
                                add.setClickable(false);
                            }
                            else {
                                add.setBackgroundColor(Color.parseColor("#3F51B5"));
                                add.setClickable(true);
                            }
                        }
                    }
                });
                if(paymentMode == 1){
                    relativeLayoutCheckbox.setVisibility(View.GONE);
                    paid = 1;
                }
                else
                    relativeLayoutCheckbox.setVisibility(View.VISIBLE);
                //calls completedAPI
                finalPaid = paid;
                complete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //checks for credit
                        if(paymentMode == 0 && paymentRecieved.isChecked()){
                            finalPaid = 1;
                        }
                        else if(paymentMode == 0 && !paymentRecieved.isChecked())
                            finalPaid = 0;
                        dialog.cancel();
                        completedProgressDialog.setMessage("Loading");
                        completedProgressDialog.setCancelable(false);
                        completedProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        completedProgressDialog.show();
                        completedAPICall(returnQuantity.getText().toString(), finalPaid);
                    }
                });
            }
        });

        //calls cancelAPI after getting reason from dialog
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.cancel_layout);
                dialog.show();
                TextView name = (TextView) dialog.findViewById(R.id.name);
                TextView address = (TextView) dialog.findViewById(R.id.address);
                Button confirm = (Button) dialog.findViewById(R.id.confirm_cancellation);
                Button close = (Button) dialog.findViewById(R.id.close);
                final RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);

                name.setText(userName);
                address.setText(userAddress);

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RadioButton radioButton = (RadioButton) dialog.findViewById(radioGroup.getCheckedRadioButtonId());
                        reason = (String) radioButton.getText();
                        dialog.cancel();
                        cancelAPICall();
                    }
                });

                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

            }
        });

        return view;
    }

    private void cancelAPICall() {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("reason", reason);
        client.setTimeout(timeout);
        client.addHeader("auth-token", Utilities.getFromeUserDefault(getContext(), Constant.AUTH_TOKEN));
        client.post(Constant.BASE_URL + "order/" + Utilities.getFromeUserDefault(getContext(), Constant.CURRENT_DELIV_ID)
                + "/cancelled", params, new FragmentRoute_Map.cancelResponseHandlerClass());

    }

    private void completedAPICall(String retCans,int paid) {

        Log.e(TAG,"payment_received:"+paid);
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(timeout);
        RequestParams params = new RequestParams();
        params.add("returned_cans",retCans);
        params.add("payment_received",""+paid);
        client.addHeader("auth-token", Utilities.getFromeUserDefault(getContext(), Constant.AUTH_TOKEN));
        client.put(Constant.BASE_URL + "order/" + Utilities.getFromeUserDefault(getContext(), Constant.CURRENT_DELIV_ID) + "/delivered",params,
                new FragmentRoute_Map.completedResponseHandlerClass());
    }

    //method to get permission to call customer
    private void calluser() {

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
            callPerson();
        }
    }

    //gets customer phone number and calls it
    private void callPerson() {

        try {
            Intent phoneIntent = new Intent(Intent.ACTION_CALL);
            phoneIntent.setData(Uri.parse("tel:" + number));
            startActivity(phoneIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //API to get all the delivery details of the customer
    private void loadDetails() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("auth-token", Utilities.getFromeUserDefault(getContext(), Constant.AUTH_TOKEN));
        client.setTimeout(timeout);
        client.get(Constant.BASE_URL + "order/" + Utilities.getFromeUserDefault(getContext(), Constant.CURRENT_DELIV_ID)
                , new FragmentRoute_Map.responseHandlerClass());
    }

    //initializing map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private void callMap() {

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Obtaining current location!");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        GPSTracker locationClass = new GPSTracker(getContext());

        if (locationClass.getLocation() != null && latitude != 0.0) {
            getDistanceTime();
        }

    }

    //DistanceMatrixAPI call
    private void getDistanceTime() {

        GPSTracker locationClass = new GPSTracker(getContext());

        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + locationClass.getLatitude() + "," + locationClass.getLongitude() + "&destinations=" + latitude + "," +
                longitude + "&mode=driving&language=fr-FR&avoid=tolls&key=AIzaSyDX66bGiTmUyP5dukJ-ngXb2_syJjSwIL4";

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(timeout);
        client.get(url, new FragmentRoute_Map.responseHandlerDistanceMatrixClass());

    }

    private String getRequestUrl(LatLng origin, LatLng dest) {
        //Value of origin
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=driving";
        //Build the full param
        String param = str_org + "&" + str_dest + "&" + sensor + "&" + mode;
        //Output format
        String output = "json";
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        return url;
    }

    //response handler for loaddetails
    public class responseHandlerClass extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            ModelCurrentDelivery model = new Gson().fromJson(new String(responseBody), ModelCurrentDelivery.class);
            if (model.success == 1) {
                //sets all the values in the respective textviews
                address.setText(model.order_details.get(0).delivery_address);
                name.setText(model.order_details.get(0).userDetails.firstName + " " +
                        model.order_details.get(0).userDetails.lastName);
                amount.setText("Rs."+model.order_details.get(0).price+"  "+model.order_details.get(0).payment_mode);
                if(model.order_details.get(0).payment_mode.toLowerCase().equals("wallet")){
                    paymentMode = 1;
                }else{
                    paymentMode = 0;
                }
                cansReturn.setText("Returns | " + model.order_details.get(0).return_cans);
                returnQuant = model.order_details.get(0).return_cans;
                userAddress = model.order_details.get(0).delivery_address;
                userName = model.order_details.get(0).userDetails.firstName + " " + model.order_details.get(0).userDetails.lastName;
                number = model.order_details.get(0).userDetails.phone;
                phone.setText(model.order_details.get(0).userDetails.phone);
                quantity.setText("Delivery | " + model.order_details.get(0).quantity + " cans");
                latitude = model.order_details.get(0).latitude;
                longitude = model.order_details.get(0).longitude;

                //calls callmap method for routemap
                callMap();
                progressDialog.dismiss();
            } else {
                if (model.error_code == 404 || model.error_code == 450) {
                    //logsout if auth-token is mismatched
                    Utilities.saveToUserDefault(getContext(), Constant.VEHICLE_ID, "");
                    Utilities.saveToUserDefault(getContext(), Constant.DRIVER_ID, "");
                    Utilities.saveToUserDefault(getContext(), Constant.AUTH_TOKEN, "");
                    Utilities.saveToUserDefault(getContext(), Constant.VEHICLE_CAPACITY, "");
                    Utilities.saveToUserDefault(getContext(), Constant.CURRENT_DELIV_ID, "");
                    Utilities.saveToUserDefault(getContext(), Constant.CURRENT_DELIV_STATUS, "");

                    startActivity(new Intent(getContext(), LoginActivity.class));
                    progressDialog.dismiss();
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(getContext(), "Seems like your network connectivity is down or very slow!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                callPerson();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) getContext(), Manifest.permission.CALL_PHONE)) {
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Need Call Permission");
                    builder.setMessage("W2D needs Call Permission");
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
                    Toast.makeText(getActivity(), "Unable to get Permission", Toast.LENGTH_LONG).show();
                }
            }
        }
        else if (locationRequestCode == 500){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                turnGPS();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //Show Information about why you need the permission

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Need Location Permission");
                    builder.setMessage("Legal Guru needs Location Permission");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

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
                    Toast.makeText(getContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class completedResponseHandlerClass extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            ModelOrderCompleted model = new Gson().fromJson(new String(responseBody), ModelOrderCompleted.class);
            if (model.success == 1) {
                //clears all the temporary variables and required sharedprefs
                completedProgressDialog.dismiss();
                Toast.makeText(getContext(), "The order has been completed successfully!", Toast.LENGTH_SHORT).show();
                Utilities.saveToUserDefault(getContext(), Constant.CURRENT_DELIV_ID, "");
                Utilities.saveToUserDefault(getContext(), Constant.CURRENT_DELIV_STATUS, "0");
                latitude = 0.0;
                longitude = 0.0;
                mMap.clear();
                temp=0;
                flag=0;
                viewPager.setCurrentItem(0);
            } else {
                if (model.error_code == 404 || model.error_code == 450) {
                    //logsout if auth-token is mismatched
                    Utilities.saveToUserDefault(getContext(), Constant.VEHICLE_ID, "");
                    Utilities.saveToUserDefault(getContext(), Constant.DRIVER_ID, "");
                    Utilities.saveToUserDefault(getContext(), Constant.AUTH_TOKEN, "");
                    Utilities.saveToUserDefault(getContext(), Constant.VEHICLE_CAPACITY, "");
                    Utilities.saveToUserDefault(getContext(), Constant.CURRENT_DELIV_ID, "");
                    Utilities.saveToUserDefault(getContext(), Constant.CURRENT_DELIV_STATUS, "");

                    startActivity(new Intent(getContext(), LoginActivity.class));
                    progressDialog.dismiss();
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), model.message, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(getContext(), "Seems like your network connectivity is down or very slow!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!Utilities.getFromeUserDefault(getContext(), Constant.CURRENT_DELIV_ID).isEmpty()) {
            SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);

            loadDetails();
        } else {
            rlContent.setVisibility(View.GONE);
            rlContentDetails.setVisibility(View.GONE);
            rlNoDetails.setVisibility(View.VISIBLE);
            progressDialog.dismiss();
        }
    }

    public class cancelResponseHandlerClass extends AsyncHttpResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            ModelOrderCancelled model = new Gson().fromJson(new String(responseBody), ModelOrderCancelled.class);
            if (model.success == 1) {
                //clears all the temporary variables and required sharedprefs
                Toast.makeText(getContext(), "The order has been cancelled successfully!", Toast.LENGTH_SHORT).show();
                Utilities.saveToUserDefault(getContext(), Constant.CURRENT_DELIV_ID, "");
                Utilities.saveToUserDefault(getContext(), Constant.CURRENT_DELIV_STATUS, "0");
                duration = "";
                distance = "";
                latitude = 0.0;
                longitude = 0.0;
                reason = "";
                temp=0;
                flag=0;
                mMap.clear();
                viewPager.setCurrentItem(0);
            } else {
                if (model.error_code == 404 || model.error_code == 450) {
                    //logsout if auth-token is mismatched
                    Utilities.saveToUserDefault(getContext(), Constant.VEHICLE_ID, "");
                    Utilities.saveToUserDefault(getContext(), Constant.DRIVER_ID, "");
                    Utilities.saveToUserDefault(getContext(), Constant.AUTH_TOKEN, "");
                    Utilities.saveToUserDefault(getContext(), Constant.VEHICLE_CAPACITY, "");
                    Utilities.saveToUserDefault(getContext(), Constant.CURRENT_DELIV_ID, "");
                    Utilities.saveToUserDefault(getContext(), Constant.CURRENT_DELIV_STATUS, "");

                    startActivity(new Intent(getContext(), LoginActivity.class));
                    progressDialog.dismiss();
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(getContext(), "Seems like your network connectivity is down or very slow!", Toast.LENGTH_SHORT).show();
        }
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }
    //draws route map
    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                Log.e(TAG, "TaskRequestDirectionsException");
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }
    //draws routemap
    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                Log.e(TAG, "TaskParserException");
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                polylineOptions = new PolylineOptions();
                points = new ArrayList();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat, lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(R.color.tabselectedcolour);
                polylineOptions.geodesic(true);
            }

            try {
                if (polylineOptions != null) {
                    mMap.addPolyline(polylineOptions);
                } else {
                    mMap.clear();
                    getpolyline(latLng,destination);
                }
            } catch (NullPointerException e) {
                try {
                    mMap.clear();
                    getpolyline(latLng,destination);
                } catch (Exception e1) {
                    getpolyline(latLng,destination);
                }
            }
        }
    }


    public class responseHandlerDistanceMatrixClass extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            ModelDistanceMatrix model = new Gson().fromJson(new String(responseBody), ModelDistanceMatrix.class);
            distance = model.rows.get(0).elements.get(0).distance.text;
            duration = model.rows.get(0).elements.get(0).duration.text;

            try{
                routeMap();
            } catch (Exception e){

            }

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(getContext(), "Seems like your network connectivity is down or very slow!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void routeMap() {

        GPSTracker locationClass = new GPSTracker(getContext());

        LatLng source = new LatLng(locationClass.getLatitude(),locationClass.getLongitude());
        destination = new LatLng(latitude,longitude);

        Location prevLocation = new Location("provider");
        prevLocation.setLatitude(source.latitude);
        prevLocation.setLongitude(source.longitude);

        int height = 80;
        int width = 80;

        //setting marker icon
        int truckwidth = 40;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.truck);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap iconTruck = Bitmap.createScaledBitmap(b, truckwidth, height, false);

        //setting marker icon
        BitmapDrawable bitmapdraww=(BitmapDrawable)getResources().getDrawable(R.drawable.home);
        Bitmap bitmap=bitmapdraww.getBitmap();
        Bitmap iconHome = Bitmap.createScaledBitmap(bitmap, width, height, false);

            try{
                if(distance!=""){
                    //adds markers on the map
                    mMap.addMarker(new MarkerOptions().position(source).title("Distance:"+ distance.replace(",","."))
                            .icon(BitmapDescriptorFactory.fromBitmap(iconTruck)).rotation(prevLocation.getBearing()));
                    mMap.addMarker(new MarkerOptions().position(destination).title("Duration:" + duration).icon(BitmapDescriptorFactory.fromBitmap(iconHome)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(source));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationClass.getLatitude(),
                            locationClass.getLongitude()), 15f));
                }
                else{
                    mMap.clear();
                    callMap();
                }
            }
            catch (NullPointerException e){
                mSupportMapFragment.getMapAsync(this);
                rlContentDetails.setVisibility(View.VISIBLE);
                routeMap();
            }

            temp = 1;

            String url = getRequestUrl(source,destination);
            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
            taskRequestDirections.execute(url);

            //if current location changes,this method is called and it sets the marker accordingly
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1500, 25, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        currentLocation = location;

                        latLng = new LatLng(location.getLatitude(),location.getLongitude());

                        CameraPosition camPos = new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                .bearing(location.getBearing()).zoom(15f).build();

                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(latLng).title("Distance:"+ distance.replace(",","."))
                                        .icon(BitmapDescriptorFactory.fromBitmap(iconTruck)));
                        mMap.addMarker(new MarkerOptions().position(destination).title("Duration:" + duration).icon(BitmapDescriptorFactory.fromBitmap(iconHome)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));

                        //calls getpolyline to draw the routemap
                        getpolyline(latLng,destination);

                        //pushes new latlong to firebase
                        Map<String,Object> map = new HashMap();
                        map.put("latitude",location.getLatitude());
                        map.put("longitude",location.getLongitude());

                        root.updateChildren(map);

                    }
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }
        );
    }

    private void getpolyline(LatLng latLng , LatLng destination ) {

        if(latLng!=null){

            try{
                mMap.clear();

                int height = 80;
                int width = 80;

                int truckwidth = 40;

                //setting marker icon
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.truck);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap iconTruck = Bitmap.createScaledBitmap(b, truckwidth, height, false);

                //setting marker icon
                BitmapDrawable bitmapdraww=(BitmapDrawable)getResources().getDrawable(R.drawable.home);
                Bitmap bitmap=bitmapdraww.getBitmap();
                Bitmap iconHome = Bitmap.createScaledBitmap(bitmap, width, height, false);

                CameraPosition camPos = new CameraPosition.Builder()
                        .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                        .bearing(currentLocation.getBearing()).zoom(15f).build();


                mMap.addMarker(new MarkerOptions().position(latLng).title("Distance:"+ distance.replace(",","."))
                        .icon(BitmapDescriptorFactory.fromBitmap(iconTruck)));
                mMap.addMarker(new MarkerOptions().position(destination).title("Duration:" + duration).icon(BitmapDescriptorFactory.fromBitmap(iconHome)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));


                String url = getRequestUrl(latLng,destination);
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);

            }catch(Exception e){

            }

        }
        else{

            try{
                GPSTracker tracker = new GPSTracker(getContext());

                LatLng temp = new LatLng(tracker.getLatitude(),tracker.getLongitude());
                mMap.clear();

                int height = 80;
                int width = 80;

                int truckwidth = 40;

                CameraPosition camPos = new CameraPosition.Builder()
                        .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                        .bearing(currentLocation.getBearing()).zoom(15f).build();

                //setting marker icon
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.truck);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap iconTruck = Bitmap.createScaledBitmap(b, truckwidth, height, false);

                //setting marker icon
                BitmapDrawable bitmapdraww=(BitmapDrawable)getResources().getDrawable(R.drawable.home);
                Bitmap bitmap=bitmapdraww.getBitmap();
                Bitmap iconHome = Bitmap.createScaledBitmap(bitmap, width, height, false);

                mMap.addMarker(new MarkerOptions().position(temp).title("Distance:"+ distance.replace(",","."))
                        .icon(BitmapDescriptorFactory.fromBitmap(iconTruck)));
                mMap.addMarker(new MarkerOptions().position(destination).title("Duration:" + duration).icon(BitmapDescriptorFactory.fromBitmap(iconHome)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(temp));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(temp, 15f));
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));


                String url = getRequestUrl(temp,destination);
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);

            }catch(Exception e){
                GPSTracker tracker = new GPSTracker(getContext());

                LatLng temp = new LatLng(tracker.getLatitude(),tracker.getLongitude());
                mMap.clear();

                int height = 80;
                int width = 80;

                int truckwidth = 40;

                //setting marker icon
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.truck);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap iconTruck = Bitmap.createScaledBitmap(b, truckwidth, height, false);

                //setting marker icon
                BitmapDrawable bitmapdraww=(BitmapDrawable)getResources().getDrawable(R.drawable.home);
                Bitmap bitmap=bitmapdraww.getBitmap();
                Bitmap iconHome = Bitmap.createScaledBitmap(bitmap, width, height, false);

                mMap.addMarker(new MarkerOptions().position(temp).title("Distance:"+ distance.replace(",","."))
                        .icon(BitmapDescriptorFactory.fromBitmap(iconTruck)));
                mMap.addMarker(new MarkerOptions().position(destination).title("Duration:" + duration).icon(BitmapDescriptorFactory.fromBitmap(iconHome)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(temp));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(temp, 15f));

                String url = getRequestUrl(temp,destination);
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);

            }
        }
    }

        //gets permission for gps
        private void turnGPS(){
        GPSTracker gps = new GPSTracker(getContext());

        if(!gps.canGetLocation()){
            gps.showSettingsAlert();
        }
    }
}
