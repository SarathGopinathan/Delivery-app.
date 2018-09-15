package com.example.shadesix.w2d.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.shadesix.w2d.Adapters.AdapterDelivered;
import com.example.shadesix.w2d.Adapters.AdapterRemaining;
import com.example.shadesix.w2d.LoginActivity;
import com.example.shadesix.w2d.Models.ModelCurrentDelivery;
import com.example.shadesix.w2d.Models.ModelDelivered;
import com.example.shadesix.w2d.Models.ModelDeliveredDetails;
import com.example.shadesix.w2d.Models.ModelRemaining;
import com.example.shadesix.w2d.R;
import com.example.shadesix.w2d.Utils.Constant;
import com.example.shadesix.w2d.Utils.Utilities;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by shade six on 1/9/2018.
 */

public class FragmentDelivered extends Fragment{
    RecyclerView recyclerView;
    int timeout = 300000;
    AdapterDelivered adapter;
    RelativeLayout rlEmpty;
    SwipeRefreshLayout swipeRefreshLayout;
    ProgressDialog progressDialog;
    String TAG = "Fragment Delivered";
    public FragmentDelivered() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivered,container,false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        rlEmpty = (RelativeLayout) view.findViewById(R.id.rl_emptylist);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_delivered);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,R.color.colorAccent,R.color.tabselectedcolour);
        //on swipe refresh the screen
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDetailList();
            }
        });

        //loads all the details
        getDetailList();

        return view;
    }

    private void getDetailList() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("auth-token", Utilities.getFromeUserDefault(getContext(), Constant.AUTH_TOKEN));
        client.setTimeout(timeout);
        client.get(Constant.BASE_URL+"orders/completed",new FragmentDelivered.responseHandlerClass());

    }

    public class responseHandlerClass extends AsyncHttpResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            ModelDelivered model = new Gson().fromJson(new String(responseBody),ModelDelivered.class);

            if(model.success==1){

                if(model.order_details.size() != 0){

                    //add values to the adapter
                    adapter = new AdapterDelivered(model.order_details);
                    recyclerView.setHasFixedSize(true);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(adapter);
                    progressDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);
                }

                else{
                    recyclerView.setVisibility(View.GONE);
                    rlEmpty.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
            else{
                if(model.error_code==404 || model.error_code==450){

                    //log out if auth-token mismatches
                    Utilities.saveToUserDefault(getContext(),Constant.VEHICLE_ID,"");
                    Utilities.saveToUserDefault(getContext(),Constant.DRIVER_ID,"");
                    Utilities.saveToUserDefault(getContext(),Constant.AUTH_TOKEN,"");
                    Utilities.saveToUserDefault(getContext(),Constant.VEHICLE_CAPACITY,"");
                    Utilities.saveToUserDefault(getContext(),Constant.CURRENT_DELIV_ID,"");
                    Utilities.saveToUserDefault(getContext(),Constant.CURRENT_DELIV_STATUS,"");

                    startActivity(new Intent(getContext(), LoginActivity.class));
                    progressDialog.dismiss();
                    getActivity().finish();
                }
                else{
                    Toast.makeText(getContext(),"Something went wrong...",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(getContext(),"Seems like your network connectivity is down or very slow!",Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
