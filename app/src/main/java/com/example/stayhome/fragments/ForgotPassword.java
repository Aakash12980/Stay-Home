package com.example.stayhome.fragments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.stayhome.LoginSignup;
import com.example.stayhome.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    private String email;
    private TextInputLayout emailView;

    public static final String TAG = "FORGOT PSWD ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailView = findViewById(R.id.email_input);
        MaterialButton submitBtn = findViewById(R.id.submit_btn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationMail();
            }
        });


    }
    private void sendVerificationMail(){
        if (validateInputs()){
            if (isNetworkAvailable()) {
                ProgressBar progressBar = new ProgressBar(getApplicationContext());
                progressBar.setVisibility(View.VISIBLE);
                FirebaseAuth auth = FirebaseAuth.getInstance();

                auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(ForgotPassword.this, "Check your email to reset password.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginSignup.class));
                            finishAffinity();
                        }else {
                            Toast.makeText(ForgotPassword.this, "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                progressBar.setVisibility(View.INVISIBLE);
            }else {
                Toast.makeText(this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private boolean validateInputs(){
        try {
            email = emailView.getEditText().getText().toString().trim().toLowerCase();
        }catch (NullPointerException e){
            email = "";
        }
        if (TextUtils.isEmpty(email)){
            emailView.setError("This field cannot be empty.");
            return false;
        }
        return true;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
