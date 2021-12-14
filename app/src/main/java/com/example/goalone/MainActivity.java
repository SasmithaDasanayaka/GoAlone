package com.example.goalone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();
        bottomNavigationView.setSelectedItemId(R.id.homeFragment);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        switch (item.getItemId()) {
                    case R.id.homeFragment:
                        fragment = new HomeFragment();
//                        bottomNavigationView.setSelectedItemId(R.id.homeFragment);
                        break;
                    case R.id.logsFragment:
                        fragment = new LogsFragment();
//                        bottomNavigationView.setSelectedItemId(R.id.logsFragment);
                        break;
                    case R.id.settingsFragment:
                        fragment = new SettingsFragment();
//                        bottomNavigationView.setSelectedItemId(R.id.settingsFragment);
                        break;
                }
                transaction.replace(R.id.nav_host_fragment, fragment).commit();
                return true;
            }
        });

    }

}