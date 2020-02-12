package com.ourride.driver.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ourride.driver.R;
import com.ourride.driver.pojo.WingmenListResult;

import java.util.List;

public class WingmenListAdapter extends RecyclerView.Adapter<WingmenListAdapter.MyViewHolder>{

    private Context context;
    private List<WingmenListResult> wingmenList;

    public WingmenListAdapter(Context context, List<WingmenListResult> wingmenList) {
        this.context = context;
        this.wingmenList = wingmenList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView wingmenname, distance, duration;

        public MyViewHolder(View itemView) {
            super(itemView);
            wingmenname = itemView.findViewById(R.id.wingman_name);
            distance = itemView.findViewById(R.id.distancevalue);
            duration = itemView.findViewById(R.id.durationvalue);
        }
    }

    @NonNull
    @Override
    public WingmenListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_wingman,parent,false);
        return new WingmenListAdapter.MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull WingmenListAdapter.MyViewHolder holder, int position) {
        final WingmenListResult wingman = wingmenList.get(position);
        holder.wingmenname.setText(wingman.getName());
        holder.distance.setText(wingman.getDistance());
        holder.duration.setText(wingman.getDuration());
    }

    @Override
    public int getItemCount() {
        return wingmenList.size();
    }
}
