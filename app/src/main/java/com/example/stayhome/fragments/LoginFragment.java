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
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment{

    private String email;
    private String password;
    private MaterialButton loginBtn;
    private ProgressBar progressBar;
    private static final String TAG = "LOGIN Fragment";

    private View rootView;
    private TextInputLayout emailView;
    private TextInputLayout passwordView;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private OnFragmentInteractionListener mListener;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        emailView = (TextInputLayout) rootView.findViewById(R.id.login_email);
        passwordView = (TextInputLayout) rootView.findViewById(R.id.login_password);
        loginBtn = (MaterialButton) rootView.findViewById(R.id.login_button);
//        TextView forgotPasswordView = (TextView) rootView.findViewById(R.id.forgot_password);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()){
                    signIn();
                }
            }
        });
//        forgotPasswordView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(getContext(), ForgotPassword.class));
//
//            }
//        });

        return rootView;
    }

    private void signIn(){

        if (isNetworkAvailable()){
            progressBar = new ProgressBar(getContext());
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
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
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
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
            password = passwordView.getEditText().getText().toString().trim();
        }catch (NullPointerException e){
            password = "";
        }

        if (TextUtils.isEmpty(email)){
            emailView.setError("Username field Empty!");
            return false;
        }else if (TextUtils.isEmpty(password)){
            passwordView.setError("Password field Empty!");
            return false;
        }
        return true;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
