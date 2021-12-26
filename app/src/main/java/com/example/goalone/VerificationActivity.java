package com.example.goalone;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

//import com.example.goalone.fragment.HomeFragment;
import com.example.goalone.fragment.SettingsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.goalone.databinding.ActivityVerificationBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    private final static String default_notification_channel_id = "default";

    private AppBarConfiguration appBarConfiguration;
    private ActivityVerificationBinding binding;

    public static FirebaseAuth mAuth; // variable for FirebaseAuth class
    private EditText edtPhone, edtOTP, edtName;  // field for phone and OTP

    private Button verifyOTPBtn, generateOTPBtn;    // buttons for generating OTP and verifying OTP

    private String verificationId;   // string for storing verification ID
    public static String nameVal;

    public static final String USER_NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // getting instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // initializing variables for button and Edittext
        edtPhone = findViewById(R.id.idEdtPhoneNumber);
        edtOTP = findViewById(R.id.idEdtOtp);
        edtName = findViewById(R.id.idEdtName);
        verifyOTPBtn = findViewById(R.id.idBtnVerify);
        generateOTPBtn = findViewById(R.id.idBtnGetOtp);

        //onclick listener for generate OTP button
        generateOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //for checking weather the user has entered his mobile number or not
                if(TextUtils.isEmpty(edtPhone.getText().toString())){
                    Toast.makeText(VerificationActivity.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(edtName.getText().toString())) {
                    Toast.makeText(VerificationActivity.this, "Please enter the name", Toast.LENGTH_SHORT).show();
                }else
                {
                    // calling send OTP method for getting OTP from Firebase
                    String phone = edtPhone.getText().toString();
                    sendVerificationCode(phone);
                }
            }
        });

        // onClick listner for verify OTP button
        verifyOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking whether the otp field is empty
                if (TextUtils.isEmpty(edtOTP.getText().toString())) {
                    Toast.makeText(VerificationActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                }else{
                    verifyCode(edtOTP.getText().toString());
                }
            }
        });
        if(mAuth.getCurrentUser() != null){
            startMain();
        }

    }
    private void saveName(String name){
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsFragment.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_NAME, name);

        editor.apply();
    }
    public String getName(){
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsFragment.SHARED_PREFS, Context.MODE_PRIVATE);
        nameVal = sharedPreferences.getString(USER_NAME, String.valueOf(true));

        Toast.makeText(VerificationActivity.this, "Hi, "+nameVal+"!", Toast.LENGTH_LONG).show();

        return nameVal;
    }
    private void signInWithCredential(PhoneAuthCredential credential){
        //checking OTP is correct or not
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            if(mAuth.getCurrentUser() != null){
                                saveName(edtName.getText().toString());
                                getName();
                                startMain();
                            }
                        }else{
                            Toast.makeText(VerificationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void startMain(){
        Intent i = new Intent(VerificationActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    private void sendVerificationCode(String number){
        //getting OTP on user phone number
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(number)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallBack)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    //callback method is called on Phone auth provider
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        //this method is used when the OTP is sent from Firebase
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken){
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        // when the user receive OTP from Firebase
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
        // getting OTP code which is sent in phone auth credentials
            final String code = phoneAuthCredential.getSmsCode();
            //checking the code
            if(code != null){
                edtOTP.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(VerificationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    // verify code from Firebase
    private void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        //sign in method
        signInWithCredential(credential);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_verification);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}