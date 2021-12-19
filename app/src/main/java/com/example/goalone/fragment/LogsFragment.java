package com.example.goalone.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.goalone.MyAdapter;
import com.example.goalone.R;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LogsFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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
        warningMsg = new String[]{"High risk, danger", "Warning", "Caution", "Safer","Warning"};
        time = new String[]{"10.00 am", "11.00 am", "11.10 am", "11.25 am", "11.45 am"};
        images = new int[warningMsg.length];
        for(int i = 0; i < warningMsg.length; i++){
            if(warningMsg[i].equals("High risk, danger")){
                images[i] = R.drawable.danger;
            }else if(warningMsg[i].equals("Warning")){
                images[i] = R.drawable.warning;
            }else if(warningMsg[i].equals("Caution")){
                images[i] = R.drawable.caution;
            }else if(warningMsg[i].equals("Safer")){
                images[i] = R.drawable.safer;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_logs, container, false);

        recyclerView = v.findViewById(R.id.recyclerView);

        MyAdapter myAdapter = new MyAdapter(this.getContext(), warningMsg, time, images);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        return v;
    }
}