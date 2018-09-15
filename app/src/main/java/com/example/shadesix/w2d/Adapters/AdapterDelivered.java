package com.example.shadesix.w2d.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shadesix.w2d.Models.ModelCurrentDelivery;
import com.example.shadesix.w2d.Models.ModelCurrentDeliveryDetails;
import com.example.shadesix.w2d.Models.ModelDelivered;
import com.example.shadesix.w2d.Models.ModelDeliveredDetails;
import com.example.shadesix.w2d.Models.ModelRemaining;
import com.example.shadesix.w2d.R;

import java.util.List;

/**
 * Created by shade six on 1/9/2018.
 */

public class AdapterDelivered extends RecyclerView.Adapter<AdapterDelivered.MyViewHolder>{

    private List<ModelDeliveredDetails> list;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView name,address,price,returnedCans;
        public ImageView del_status,watercan;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            address = (TextView) itemView.findViewById(R.id.address);
            price = (TextView) itemView.findViewById(R.id.price);
            returnedCans = (TextView) itemView.findViewById(R.id.cans);
            del_status = (ImageView) itemView.findViewById(R.id.del_status);
            watercan = (ImageView) itemView.findViewById(R.id.watercan);
        }
    }

    public AdapterDelivered(List<ModelDeliveredDetails> list) {
        this.list = list;
    }

    @Override
    public AdapterDelivered.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_delivered,parent,false);
        return new AdapterDelivered.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AdapterDelivered.MyViewHolder holder, int position) {

        ModelDeliveredDetails modelRemaining = list.get(position);
        holder.price.setText("Rs."+modelRemaining.price+" "+modelRemaining.payment_mode);
        holder.returnedCans.setText("Returned cans: "+modelRemaining.returned_cans);
        holder.name.setText(modelRemaining.first_name+" "+modelRemaining.last_name);
        holder.address.setText(modelRemaining.delivery_address);
        if(modelRemaining.order_status == 4){
            holder.watercan.setImageResource(R.drawable.icon_watercan);
            holder.del_status.setImageResource(R.drawable.order_delivered);
        }
        else if(modelRemaining.order_status == 0){
            holder.watercan.setImageResource(R.drawable.cancel_watercan);
            holder.del_status.setImageResource(R.drawable.order_cancel);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
