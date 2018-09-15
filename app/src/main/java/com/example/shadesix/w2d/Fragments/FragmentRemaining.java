package com.example.shadesix.w2d.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.shadesix.w2d.Adapters.AdapterRemaining;
import com.example.shadesix.w2d.LoginActivity;
import com.example.shadesix.w2d.Models.ModelDispatched;
import com.example.shadesix.w2d.Models.ModelDistanceMatrix;
import com.example.shadesix.w2d.Models.ModelRemaining;
import com.example.shadesix.w2d.Models.ModelRemainingDetails;
import com.example.shadesix.w2d.R;
import com.example.shadesix.w2d.Utils.Constant;
import com.example.shadesix.w2d.Utils.GPSTracker;
import com.example.shadesix.w2d.Utils.RecyclerTouchListener;
import com.example.shadesix.w2d.Utils.Utilities;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.Collections;

import cz.msebera.android.httpclient.Header;

import static com.example.shadesix.w2d.LandingActivity.viewPager;

/**
 * Created by shade six on 1/9/2018.
 */

public class FragmentRemaining extends Fragment{
    RecyclerView recyclerView;
    int timeout = 300000;
    public int maxSize=0,tempSize=0,index=0;
    SwipeRefreshLayout swipeRefreshLayout;
    public AdapterRemaining adapter;
    public ArrayList<Float> distanceList = new ArrayList();
    public ArrayList<ModelRemainingDetails> sortedList = new ArrayList();
    ProgressDialog progressDialog,sortProgressDialog;
    RelativeLayout rlEmptyList;
    String TAG = "Fragment Remaining";
    public FragmentRemaining() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remaining,container,false);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_remaining);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        rlEmptyList = (RelativeLayout) view.findViewById(R.id.rl_emptylist);
        sortProgressDialog = new ProgressDialog(getContext());
        sortProgressDialog.setCancelable(false);
        sortProgressDialog.setMessage("Sorting!");
        sortProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,R.color.colorAccent,R.color.tabselectedcolour);
        //on swipe refresh screen
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                maxSize=tempSize=0;
                sortedList.clear();
                getRemainingList();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    if(Utilities.getFromeUserDefault(getContext(),Constant.CURRENT_DELIV_STATUS).equals("0")){
                        //sort if in first fragment and if the user as finished/cancelled a delivery
                        progressDialog.show();
                        //get the list from API
                        getRemainingList();
                        Utilities.saveToUserDefault(getContext(),Constant.CURRENT_DELIV_STATUS,"1");
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }

    private void getRemainingList() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30*10000);
        client.addHeader("auth-token",Utilities.getFromeUserDefault(getContext(),Constant.AUTH_TOKEN));
        client.get(Constant.BASE_URL+"orders",new FragmentRemaining.ResponceHandlingClass());
    }

    public class ResponceHandlingClass extends AsyncHttpResponseHandler {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            ModelRemaining model = new Gson().fromJson(new String(responseBody),ModelRemaining.class);
            if(model.success == 1){
                if(model.ordersList.size()!=0){
                    //if response arraylist is not empty,make the empty list icon view as gone and add the arraylist to the adapter and to the recyclerview
                    rlEmptyList.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    adapter = new AdapterRemaining(model.ordersList);

                    recyclerView.setHasFixedSize(true);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(adapter);

                    progressDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);

                    basesort(model.ordersList);

                }
                else{
                    //response arraylist empty so set emptylist icon view as visible and recycler view visiblity as gone
                    recyclerView.setVisibility(View.GONE);
                    rlEmptyList.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
            else{
                if(model.error_code==404 || model.error_code==450){

                    //if auth token is mismatched logout

                    Utilities.saveToUserDefault(getContext(),Constant.VEHICLE_ID,"");
                    Utilities.saveToUserDefault(getContext(),Constant.DRIVER_ID,"");
                    Utilities.saveToUserDefault(getContext(),Constant.AUTH_TOKEN,"");
                    Utilities.saveToUserDefault(getContext(),Constant.VEHICLE_CAPACITY,"");
                    Utilities.saveToUserDefault(getContext(),Constant.CURRENT_DELIV_ID,"");
                    Utilities.saveToUserDefault(getContext(),Constant.CURRENT_DELIV_STATUS,"");

                    startActivity(new Intent(getContext(), LoginActivity.class));
                    getActivity().finish();
                }
                else{
                    Toast.makeText(getActivity(),"Something went wrong!",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(getActivity(),"Seems like your network connectivity is down or very slow!",Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void basesort(ArrayList<ModelRemainingDetails> list) {

        sortProgressDialog.show();

        sortedList.clear();

        //assign the respose arraylist to a locally declared arraylist
        sortedList=list;

        //get the size of the arraylist
        maxSize=list.size();

        //clear another local arraylist which contains distance values
        distanceList.clear();

        //call function which gets distance values
        distanceMatrixAPI(sortedList.get(0).latitude,sortedList.get(0).longitude,0);

    }

    void distanceMatrixAPI(String lat, String longi, int index){

        GPSTracker locationClass = new GPSTracker(getContext());

        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + locationClass.getLatitude()+","+locationClass.getLongitude() + "&destinations=" +
                lat + "," + longi + "&mode=driving&language=fr-FR&avoid=tolls&key=AIzaSyDX66bGiTmUyP5dukJ-ngXb2_syJjSwIL4";

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30*10000);
        client.get(url,new FragmentRemaining.responseHandlerDistanceMatrixClass());

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortList() {

        tempSize=0;

        //assign the distance attribute to the model after calculating the distance
        for(int j=0;j<sortedList.size();j++){
            sortedList.get(j).distance = distanceList.get(j);
        }

        //sort the arraylist ascending order based on distance attribute
        Collections.sort(sortedList);

        //set the sorted arraylist to the adapter
        AdapterRemaining adapterRemaining = new AdapterRemaining(sortedList);

        //set the adapter to the recyclerview
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapterRemaining);

        //if user clicks on the first view of the recyclerview open maps app and pass source and destination locations along with driving mode
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(position == 0) {
                    dispatchedAPI();
                    GPSTracker locationClass = new GPSTracker(getContext());
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?saddr="+locationClass.getLatitude()+","+locationClass.getLongitude()+
                                            "&daddr="+sortedList.get(0).latitude+","+sortedList.get(0).longitude+"&mode=driving"));
                    startActivity(intent);
                }
            }
            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        try{
            Utilities.saveToUserDefault(getContext(),Constant.CURRENT_DELIV_ID,sortedList.get(0).id);
        }catch (Exception exception){
        }
        sortProgressDialog.dismiss();
    }

    private void dispatchedAPI() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(timeout);
        RequestParams params = new RequestParams();
        client.addHeader("auth-token", Utilities.getFromeUserDefault(getContext(), Constant.AUTH_TOKEN));
        client.put(Constant.BASE_URL + "order/" + Utilities.getFromeUserDefault(getContext(), Constant.CURRENT_DELIV_ID) + "/dispatched",
                new FragmentRemaining.dispatchedResponseHandlerClass());

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getRemainingList();
    }

    public class responseHandlerDistanceMatrixClass extends AsyncHttpResponseHandler {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            try{

                //get the distance from the response and add it to the distance arraylist
                ModelDistanceMatrix model = new Gson().fromJson(new String (responseBody),ModelDistanceMatrix.class);
                distanceList.add(tempSize,Float.parseFloat(model.rows.get(0).elements.get(0).distance.text.replace(",",".")
                        .replace(" ","").replace("km","").replace("m","")));
                //add the counter to check if it has reached the size of the getremaininglist arraylist
                tempSize++;
                if(tempSize != maxSize){
                    //not reached the max size,call distanceMatrixAPI in recursion
                    try{
                        distanceMatrixAPI(sortedList.get(tempSize).latitude,sortedList.get(tempSize).longitude,tempSize);
                    }catch(Exception exception){
                    }
                }
                //reached max size so call sortList()
                else
                    sortList();
            }
            catch(NullPointerException e){
                distanceMatrixAPI(sortedList.get(tempSize).latitude,sortedList.get(tempSize).longitude,tempSize);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(getContext(),"Seems like your network connectivity is down or very slow!",Toast.LENGTH_SHORT).show();
        }
    }

    public class dispatchedResponseHandlerClass extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            ModelDispatched model = new Gson().fromJson(new String(responseBody), ModelDispatched.class);
            if (model.success == 1) {
//                success
            } else {
                if (model.error_code == 404 || model.error_code == 450) {
                    //logout in case of auth-token mismatch
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
                }
            }
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(getContext(), "Seems like your network connectivity is down or very slow!", Toast.LENGTH_SHORT).show();
        }
    }
}
