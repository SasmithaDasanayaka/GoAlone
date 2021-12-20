package com.example.goalone;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.goalone.databinding.ActivityVerificationBinding;
import com.google.firebase.auth.FirebaseAuth;

public class VerificationActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityVerificationBinding binding;

    private FirebaseAuth mAuth; // variable for FirebaseAuth class
    private EditText edtPhone, edtOTP;  // field for phone and OTP

    private Button verifyOTPBtn, generateOTPBtn;    // buttons for generating OTP and verifying OTP

    private String verificationId;   // string for storing verification ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // getting instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        //setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_verification);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

//        binding.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_verification);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}