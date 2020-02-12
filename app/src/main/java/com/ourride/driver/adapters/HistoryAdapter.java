package com.ourride.driver.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ourride.driver.R;
import com.ourride.driver.pojo.HistoryListResult;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private Context context;
    private List<HistoryListResult> historyList;

    public HistoryAdapter(Context context, List<HistoryListResult> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView bookingDate, bookingTime,pickFrom,dropTo,vehicleNumber,assignedDriver;
        public MyViewHolder(View itemView) {
            super(itemView);
            bookingDate = itemView.findViewById(R.id.bookingDate);
            bookingTime = itemView.findViewById(R.id.bookingTime);
            pickFrom = itemView.findViewById(R.id.pickFrom);
            dropTo = itemView.findViewById(R.id.dropTo);
            vehicleNumber = itemView.findViewById(R.id.vehicleNumber);
            assignedDriver = itemView.findViewById(R.id.assignedUser);
        }
    }


    @NonNull
    @Override
    public HistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_history,parent,false);
        return new HistoryAdapter.MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.MyViewHolder holder, int position) {
        final HistoryListResult history = historyList.get(position);
        holder.bookingDate.setText(history.getBookingDate());
        holder.bookingTime.setText(history.getBookingTime());
        holder.pickFrom.setText(history.getPickFrom());
        holder.dropTo.setText(history.getDropTo());
        holder.vehicleNumber.setText(history.getVehicleNumber());
        holder.assignedDriver.setText(history.getAssignedUser());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
