package com.example.goalone.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.goalone.Model.Device;
import com.example.goalone.R;
import com.example.goalone.adapter.DeviceAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LogsFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<Device> deviceArrayList = new ArrayList<>();
    RecyclerView recyclerView;

    private String[] warningMsg, time;
    private int images[];

    public LogsFragment() {
        // Required empty public constructor
    }

    public static LogsFragment newInstance(String param1, String param2) {
        LogsFragment fragment = new LogsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //read history data from  database
        warningMsg = new String[]{"High risk, danger", "Warning", "Safer"};
        time = new String[]{"10.00 am", "11.00 am", "11.10 am"};
        images = new int[warningMsg.length];
        for (int i = 0; i < warningMsg.length; i++) {
            if (warningMsg[i].equals("High risk, danger")) {
                images[i] = R.drawable.high;
            } else if (warningMsg[i].equals("Warning")) {
                images[i] = R.drawable.medium;
            } else if (warningMsg[i].equals("Safer")) {
                images[i] = R.drawable.low;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_logs, container, false);

        recyclerView = v.findViewById(R.id.recyclerView);
        DeviceAdapter deviceAdapter = new DeviceAdapter(deviceArrayList);
//        MyAdapter myAdapter = new MyAdapter(this.getContext(), warningMsg, time, images);
        recyclerView.setAdapter(deviceAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        FirebaseDatabase d = FirebaseDatabase.getInstance();
        d.getReference().child("recent").child(FirebaseAuth
                .getInstance()
                .getCurrentUser()
                .getPhoneNumber()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    for (DataSnapshot dataSnapshot :
                            Objects.requireNonNull(task.getResult()).getChildren()) {
                        deviceArrayList.add(dataSnapshot.getValue(Device.class));
                    }
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        });
        return v;
    }
}