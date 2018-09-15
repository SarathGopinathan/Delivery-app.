package com.example.shadesix.w2d.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.shadesix.w2d.Models.ModelRemaining;
import com.example.shadesix.w2d.Models.ModelRemainingDetails;
import com.example.shadesix.w2d.R;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by shade six on 1/9/2018.
 */

public class AdapterRemaining extends RecyclerView.Adapter<AdapterRemaining.MyViewHolder>{

    private List<ModelRemainingDetails> list;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView name,address,quantity;
        public ImageView watercan;
        public RelativeLayout navigate;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            address = (TextView) itemView.findViewById(R.id.address);
            quantity = (TextView) itemView.findViewById(R.id.quantity);
            watercan = (ImageView) itemView.findViewById(R.id.watercan);
            navigate = (RelativeLayout) itemView.findViewById(R.id.rl_navigate);
        }
    }

    public AdapterRemaining(List<ModelRemainingDetails> list) {
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_remaining,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        ModelRemainingDetails modelRemainingDetails = list.get(position);
        if (position == 0){
            holder.navigate.setVisibility(View.VISIBLE);
        }
        holder.name.setText(modelRemainingDetails.userModel.firstName+" "+modelRemainingDetails.userModel.lastName);
        holder.address.setText(modelRemainingDetails.delivery_address);
        holder.quantity.setText(modelRemainingDetails.quantity);
        holder.watercan.setImageResource(R.drawable.icon_watercan);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
