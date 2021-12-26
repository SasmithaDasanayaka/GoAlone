package com.example.goalone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.goalone.Model.Device;
import com.example.goalone.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private ArrayList<Device> dataset;
    public DeviceAdapter(ArrayList<Device> devices) {
        dataset = devices;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView distanceView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            distanceView = itemView.findViewById(R.id.distance);

        }

        public TextView getDistanceView() {
            return distanceView;
        }

    }
    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return dataset.get(position).getThreatLevel().getValue();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.risk_level_1, parent, false);
                break;
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.risk_level_2, parent, false);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.risk_level_3, parent, false);
                break;
        }


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device device = dataset.get(position);
        String distance = ((double) Math.round(device.getAverageDistance()*1000)/1000)+" m";
        String user = device.getuName();
        holder.getDistanceView().setText(user);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}