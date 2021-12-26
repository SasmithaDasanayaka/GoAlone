package com.example.goalone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.goalone.Model.Device;
import com.example.goalone.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceLogsAdapter extends RecyclerView.Adapter<DeviceLogsAdapter.ViewHolder> {
    private ArrayList<Device> dataset;

    public DeviceLogsAdapter(ArrayList<Device> devices) {
        dataset = devices;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView distanceView;
        private TextView dateView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            distanceView = itemView.findViewById(R.id.distance);
            dateView = itemView.findViewById(R.id.date);


        }

        public TextView getDistanceView() {
            return distanceView;
        }

        public TextView getDateView() {
            return dateView;
        }

    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return dataset.get(position).getMaxThreat().getValue();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
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
        String user = device.getuName();
        Date date = new Date(device.getLastIdentifiedTime());
        DateFormat df = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        holder.getDateView().setText(df.format(date));
        holder.getDistanceView().setText(user);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}