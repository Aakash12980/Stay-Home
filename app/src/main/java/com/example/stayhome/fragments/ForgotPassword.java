package com.example.stayhome.fragments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.stayhome.R;
import com.example.stayhome.data.ForgotPasswordData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForgotPassword extends AppCompatActivity {
    private String uname;
    private String email;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private ProgressBar progressBar;

    public static final String TAG = "FORGOT PASSWORD ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        final TextInputLayout unameView = findViewById(R.id.uname_input);
        final TextInputLayout emailView = findViewById(R.id.email_input);
        MaterialButton submitBtn = findViewById(R.id.submit_btn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    uname = unameView.getEditText().getText().toString().trim().toLowerCase();
                }catch (NullPointerException e){
                    uname = "";
                }
                try {
                    email = emailView.getEditText().getText().toString().trim().toLowerCase();
                }catch (NullPointerException e){
                    email = "";
                }

                if (TextUtils.isEmpty(uname)){
                    unameView.setError("This field cannot be empty.");
                    return;
                }else if (TextUtils.isEmpty(email)){
                    emailView.setError("This field cannot be empty.");
                    return;
                }
                progressBar = new ProgressBar(getApplicationContext());
                progressBar.setVisibility(View.VISIBLE);


                databaseReference = FirebaseDatabase.getInstance().getReference("ForgotPassword").child(uname);
                valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            ForgotPasswordData data = dataSnapshot.getValue(ForgotPasswordData.class);
                            try {
                                String userEmail = data.getEmail();
                                if (userEmail.equals(email)){
                                    String tempKey = String.valueOf(data.getTempKey());
                                    try {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        openDialog(uname);
                                    }catch (android.content.ActivityNotFoundException ex){
                                        Toast.makeText(getApplicationContext(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
                                    }

                                }else{
                                    emailView.setError("The email does not belong to this user.");
                                    progressBar.setVisibility(View.INVISIBLE);
                                    return;
                                }
                            }catch (NullPointerException e){
                                Toast.makeText(ForgotPassword.this, "Request failed! This user has not set email.", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Toast.makeText(ForgotPassword.this, "Request failed! This user has not set email.", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            return;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
    private void openDialog(String uname){
        Bundle bundle = new Bundle();
        bundle.putString("uname", uname);
        UpdateField updateField = new UpdateField();
        updateField.setArguments(bundle);
        updateField.show(getSupportFragmentManager(), "forgot");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            databaseReference.removeEventListener(valueEventListener);
        }catch (NullPointerException e){
            Log.d("Forgot Password", "onDestroyView: Listener not destroyed.");
        }
    }
}
