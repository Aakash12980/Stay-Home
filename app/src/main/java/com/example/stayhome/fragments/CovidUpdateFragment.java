package com.example.stayhome.fragments;

import android.content.Context;
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
    private TextView totalCases;
    private TextView totalDeaths;
    private TextView totalRecovered;
    private TextView positive;
    private TextView quarantined;
    private TextView isolation;
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

        totalCases = rootView.findViewById(R.id.total_case_number);
        totalDeaths = rootView.findViewById(R.id.total_deaths_number);
        totalRecovered = rootView.findViewById(R.id.total_recovered_number);
        isolation = rootView.findViewById(R.id.isolation_number);
        quarantined = rootView.findViewById(R.id.quarantined_number);
        positive = rootView.findViewById(R.id.positive_numer);

        getData();

        return rootView;
    }
    public void getData(){
       if (isNetworkAvailable()){
           progressBar = new ProgressBar(getContext());
           progressBar.setVisibility(View.VISIBLE);
           try {
               new DataLoader(new DataLoader.AsyncResultListener() {
                   @Override
                   public void getResult(CovidData covidData) {
                       data = covidData;
                       if (data != null){
                           totalCases.setText(data.getTotalCase());
                           totalDeaths.setText(data.getTotalDeaths());
                           totalRecovered.setText(data.getTotalRecovered());
                           positive.setText(data.getPositive());
                           isolation.setText(data.getIsolation());
                           quarantined.setText(data.getQuarantined());
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

       }else {
           Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
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
    public void onStart() {
        super.onStart();
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
    }

    @Override
    public void onResume() {
        super.onResume();
    }


}
