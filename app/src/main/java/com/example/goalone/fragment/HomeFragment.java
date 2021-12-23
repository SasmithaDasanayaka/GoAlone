package com.example.goalone.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.goalone.Model.Device;
import com.example.goalone.R;
import com.example.goalone.adapter.DeviceAdapter;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    RecyclerView currentDevicesView;
    RecyclerView.Adapter adapter;
    ArrayList<Device> devices;
    boolean rippleStates;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SEARCH_STATUS = "searchStatus";


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public static HomeFragment newInstance(boolean param1) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putBoolean(SEARCH_STATUS, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rippleStates = getArguments().getBoolean(SEARCH_STATUS);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        currentDevicesView = view.findViewById(R.id.currentDevices);
        devices = new ArrayList<Device>();
        adapter = new DeviceAdapter(devices);
        currentDevicesView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        currentDevicesView.setAdapter(adapter);


        return view;
    }

    public boolean getRippleStatus(){
        return rippleStates;
    }

    public ArrayList<Device> getDevices() {
        return devices;
    }

    public void addDevice(Device device){
        devices.add(device);
        adapter.notifyDataSetChanged();
    }
    public void updateDevices(){
        adapter.notifyDataSetChanged();
    }

}