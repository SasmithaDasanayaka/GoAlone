package com.example.goalone;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    private Switch bluetooth;
    private Switch notification;
    private Switch vibrate;
    private Switch ringing;
    private Button saveBtn;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String BLUETOOTH_SWITCH = "bluetooth";
    public static final String NOTIFICATION_SWITCH = "notification";
    public static final String VIBRATE_SWITCH = "vibrate";
    public static final String RINGING_SWITCH = "ringing";

    private boolean bluetoothOnOff;
    private boolean notificationOnOff;
    private boolean vibrateOnOff;
    private boolean ringingOnOff;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



//        bluetooth.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                saveData();
//            }
//        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        bluetooth = v.findViewById(R.id.switch1);
        notification = v.findViewById(R.id.switch2);
        vibrate = v.findViewById(R.id.switch3);
        ringing = v.findViewById(R.id.switch4);

        saveBtn = v.findViewById((R.id.button));

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        loadData();
        updateViews();
        return v;
    }
    public void saveData(){
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(SHARED_PREFS, this.getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(BLUETOOTH_SWITCH, bluetooth.isChecked());
        editor.putBoolean(NOTIFICATION_SWITCH, notification.isChecked());
        editor.putBoolean(VIBRATE_SWITCH, vibrate.isChecked());
        editor.putBoolean(RINGING_SWITCH, ringing.isChecked());

        editor.apply();

        //Toast.makeText(this, "Changes are saved", Toast.LENGTH_SHORT).show();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(SHARED_PREFS, this.getContext().MODE_PRIVATE);
        bluetoothOnOff = sharedPreferences.getBoolean(BLUETOOTH_SWITCH, true);
        notificationOnOff = sharedPreferences.getBoolean(NOTIFICATION_SWITCH, true);
        vibrateOnOff = sharedPreferences.getBoolean(VIBRATE_SWITCH, true);
        ringingOnOff = sharedPreferences.getBoolean(RINGING_SWITCH, true);
    }

    public void updateViews(){
        bluetooth.setChecked(bluetoothOnOff);
        notification.setChecked(notificationOnOff);
        vibrate.setChecked(vibrateOnOff);
        ringing.setChecked(ringingOnOff);
    }
}