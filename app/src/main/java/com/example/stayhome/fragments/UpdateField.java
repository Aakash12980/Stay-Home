package com.example.stayhome.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.stayhome.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateField extends AppCompatDialogFragment {

    private String field = "";
    private String data;
    private String frag;
    private FirebaseUser user;
    private TextInputLayout passwordView;

    private static final String TAG = "DIALOG BOX SETTING";
    private TextInputLayout inputView;
    private TextView title;
    private ProgressBar progressBar;
    private String newPwd;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.update_field, null);

        MaterialButton saveBtn = view.findViewById(R.id.save_btn);
        MaterialButton cancelBtn = view.findViewById(R.id.cancel_btn);
        inputView = view.findViewById(R.id.dialog_input);
        title = view.findViewById(R.id.dialog_title);
        passwordView = view.findViewById(R.id.dialog_input_password);
        builder.setView(view);


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    if (frag.equals("New Email")){
                        changeEmail();
                    }else if (frag.equals("New Password")){
                        changePassword();
                    }else {
                        saveData();
                    }

                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return builder.create();
    }

    private void saveData(){
        DocumentReference documentReference = FirebaseFirestore.getInstance()
                .collection("ShopData").document(user.getUid());
        if (isNetworkAvailable()){
            progressBar = new ProgressBar(getContext());
            progressBar.setVisibility(View.VISIBLE);
            Map<String , Object> update = new HashMap<>();
            update.put(field, data);
            documentReference.update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: New value set for " +field);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Failed to change the value for "+ field);
                    Toast.makeText(getContext(), "Failed to change the value.", Toast.LENGTH_SHORT).show();
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
            dismiss();

        }else {
            Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean validateInputs(){
        try {
            if (frag.equals("New Password") ) {
                data = inputView.getEditText().getText().toString().trim();
            } else {
                data = inputView.getEditText().getText().toString().trim().toLowerCase();
            }
        }catch (NullPointerException e) {
            data = "";
        }
        if (frag.equals("New Email") ){
            if (!Patterns.EMAIL_ADDRESS.matcher(data).matches()){
                inputView.setError("Invalid email format.");
                return false;
            }
        }
        try {
            if (frag.equals("New Password")){
                newPwd = passwordView.getEditText().getText().toString().trim();
            }
        }catch (NullPointerException e){
            newPwd = "";
        }

        if (frag.equals("New Password")){
            if (TextUtils.isEmpty(newPwd)){
                passwordView.setError("This field is required.");
                return false;
            }else if (newPwd.length() < 8){
                passwordView.setError("Password must be at least 8 characters in length.");
                return false;
            }
        }

        if (TextUtils.isEmpty(data)) {
            inputView.setError("This field cannot be empty!");
            return false;
        }
        return true;

    }
    private void updateUI(){
        frag = getArguments().getString("frag", "name");
        getArguments().remove("frag");
        title.setText(frag);
        if (frag.equals("New Password") ){
            passwordView.setVisibility(View.VISIBLE);
            passwordView.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordView.setPasswordVisibilityToggleEnabled(false);
            inputView.setPasswordVisibilityToggleEnabled(false);
            inputView.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            inputView.getEditText().setHint("Old Password");
        }
        switch (frag){
            case "New Name":
                field = "shopName";
                break;
            case "New Contact":
                field = "contact";
                inputView.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case "New Genre":
                field = "shopGenre";
                break;
            case "New Email":
                inputView.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                changeEmail();
                break;
            case "New Password":
                field = "password";
                changePassword();
                break;
        }

    }

    private void changeEmail(){
        if (isNetworkAvailable()){
            progressBar = new ProgressBar(getContext());
            progressBar.setVisibility(View.VISIBLE);
            String email = user.getEmail();
            AuthCredential credential = EmailAuthProvider.getCredential(email, data);
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.updateEmail(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.d(TAG, "onComplete: Email address changed successfully.");
                                    Toast.makeText(getContext(), "Email changed successfully.", Toast.LENGTH_SHORT).show();
                                }else {
                                    Log.d(TAG, "onComplete: Failed to change email.");
                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else {
                        Log.d(TAG, "onComplete: Failed to change email.");
                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            progressBar.setVisibility(View.INVISIBLE);
        }else {
            Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }

    }

    private void changePassword(){
        if (isNetworkAvailable()){
            progressBar = new ProgressBar(getContext());
            progressBar.setVisibility(View.VISIBLE);
            String email = user.getEmail();
            AuthCredential credential = EmailAuthProvider.getCredential(email, data);
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        user.updatePassword(newPwd).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.d(TAG, "onComplete: Password changed successfully.");
                                    Toast.makeText(getContext(), "Email changed successfully.", Toast.LENGTH_SHORT).show();
                                }else {
                                    Log.d(TAG, "onComplete: Failed to change password.");
                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else {
                        Log.d(TAG, "onComplete: Failed to change password.");
                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
        }else {
            Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }

    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }
}
