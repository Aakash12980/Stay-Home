package com.example.stayhome.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stayhome.R;
import com.example.stayhome.data.CovidData;
import com.example.stayhome.data.DataLoader;
import com.google.android.material.navigation.NavigationView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CovidUpdateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CovidUpdateFragment} factory method to
 * create an instance of this fragment.
 */
public class CovidUpdateFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private OnFragmentInteractionListener mListener;
    private CovidData data;
    private static final String TAG = "COVID UPDATE FRAGMENT";
    private TextView totalConfirmed;
    private TextView totalDeaths;
    private TextView totalRecovered;
    private TextView newConfirmed;
    private TextView newDeaths;
    private TextView newRecovered;
    private ProgressBar progressBar;
    private TextView countryName;
    private String country;



    public CovidUpdateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView =  inflater.inflate(R.layout.fragment_covid_update, container, false);

        totalConfirmed = rootView.findViewById(R.id.total_case_number);
        totalDeaths = rootView.findViewById(R.id.total_deaths_number);
        totalRecovered = rootView.findViewById(R.id.total_recovered_number);
        newConfirmed = rootView.findViewById(R.id.new_case_number);
        newDeaths = rootView.findViewById(R.id.new_deaths_number);
        newRecovered = rootView.findViewById(R.id.new_recovered_number);
        View countrySelect = rootView.findViewById(R.id.country_select);
        countryName = rootView.findViewById(R.id.country_name);

        try {
            country = getArguments().getString("newSelection", "Worldwide");
            Log.d(TAG, "onCreateView: Country Name: "+ country);
        }catch (NullPointerException e){
            Log.d(TAG, "onCreateView: AGAIN WORLDWIDE.....");
            country = "Worldwide";
        }
        countryName.setText(country);

        if (isNetworkAvailable()){
            setCountryDetails(country);
        }else {
            Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }



        countrySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();

            }
        });

        return rootView;
    }
    public void setCountryDetails(String cName){
        progressBar = new ProgressBar(getContext());
        progressBar.setVisibility(View.VISIBLE);
        try {
            new DataLoader(country, new DataLoader.AsyncResultListener() {
                @Override
                public void getResult(CovidData covidData) {
                    data = covidData;
                    if (data != null){
                        totalConfirmed.setText(data.getTotalConfirmed());
                        totalDeaths.setText(data.getTotalDeaths());
                        totalRecovered.setText(data.getTotalRecovered());
                        newConfirmed.setText(data.getNewConfirmed());
                        newDeaths.setText(data.getNewDeaths());
                        newRecovered.setText(data.getNewRecovered());
                        progressBar.setVisibility(View.INVISIBLE);
                    }else {
                        Log.d(TAG, "onCreateView: Data is Null.................");
                    }
                }
            }).execute();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to get the data.", Toast.LENGTH_SHORT).show();
        }
    }

    public static Intent newIntent(String message) {
        Intent intent = new Intent();
        intent.putExtra("selectedCountry", message);
        return intent;
    }

    private void openDialog(){
        CountrySelect countrySelect = new CountrySelect();
        countrySelect.setTargetFragment(CovidUpdateFragment.this, 1);
        countrySelect.show(getFragmentManager(), "covid_update_frag");
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }
        if (requestCode == 1){
            try {
                country = data.getStringExtra("selectedCountry");
                Log.d(TAG, "onActivityResult: Selected Country: "+country);
                countryName.setText(country);
                if (isNetworkAvailable()){
                    setCountryDetails(country);
                }else {
                    Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }

            }catch (NullPointerException e){
                Log.d(TAG, "onActivityResult: Empty CountryName returned.");
            }

        }
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
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        NavigationView navigation = (NavigationView)getActivity().findViewById(R.id.nav_bar_home);
        Menu drawer_menu = navigation.getMenu();
        MenuItem menuItem;
        menuItem = drawer_menu.findItem(R.id.nav_bar_stats_frag);
        if(!menuItem.isChecked())
        {
            menuItem.setChecked(true);
        }
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: On Pause method is called.......");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: On Resume method has been called.......");
    }


}
