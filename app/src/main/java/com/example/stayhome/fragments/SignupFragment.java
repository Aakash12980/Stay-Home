package com.example.stayhome.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.stayhome.MainActivity;
import com.example.stayhome.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SignupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SignupFragment} factory method to
 * create an instance of this fragment.
 */
public class SignupFragment extends Fragment {
    private String email;
    private String pwd1;
    private String pwd2;
    private static final String TAG = "SIGNUP Fragment";

    private ProgressBar progressBar;
    private OnFragmentInteractionListener mListener;
    private TextInputLayout emailView;
    private TextInputLayout password1View ;
    private TextInputLayout password2View;
    private View rootView;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public SignupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_signup, container, false);
        emailView = (TextInputLayout) rootView.findViewById(R.id.signup_email);
        password1View = (TextInputLayout) rootView.findViewById(R.id.signup_password1);
        password2View = (TextInputLayout) rootView.findViewById(R.id.signup_password2);
        MaterialButton btn = (MaterialButton) rootView.findViewById(R.id.signup_button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateInputs() && validatePassword(pwd1, pwd2) ){
                    createUser();

                }else {
                    password2View.setError("Confirm your Password!");
                }

            }
        });
        return rootView;
    }

    private void createUser(){
        if (isNetworkAvailable()){
            progressBar = new ProgressBar(getContext());
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, pwd1).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG, "onComplete: User created for "+ email);
                        signIn();
                    }else {
                        Log.w(TAG, "Create user task failed: ", task.getException());
                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            progressBar.setVisibility(View.INVISIBLE);
        }else {
            Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }
    }

    private void signIn(){
        if (isNetworkAvailable()){
            progressBar = new ProgressBar(getContext());
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email, pwd1).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG, "onComplete: User logged in: "+ email);
                        startActivity(new Intent(getContext(), MainActivity.class));
                        getActivity().finishAffinity();

                    }else {
                        Log.w(TAG, "onComplete: Sign in with email failed. ",task.getException() );
                        Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }else {
            Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(){
        try {
            email = emailView.getEditText().getText().toString().trim().toLowerCase();
        }catch (NullPointerException e){
            email = "";
        }
        try {
            pwd1 = password1View.getEditText().getText().toString().trim();
        }catch (NullPointerException e){
            pwd1 = "";
        }
        try {
            pwd2 = password2View.getEditText().getText().toString().trim();
        }catch (NullPointerException e){
            pwd2 = "";
        }

        if (TextUtils.isEmpty(pwd1)){
            password1View.setError("Password is required!");
            return false;
        }else if (pwd1.length() < 8){
            password1View.setError("Password must be at least 8 characters in length.");
            return false;

        } else if (TextUtils.isEmpty(pwd2)){
            password2View.setError("Password confirmation is required.");
            return false;
        }else if (TextUtils.isEmpty(email)){
            emailView.setError("Email is required.");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailView.setError("Invalid email format.");
            return false;
        }
        return true;
    }

    public boolean validatePassword(String pwd1, String pwd2){
        return pwd1.equals(pwd2);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
