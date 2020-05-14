package com.example.stayhome.fragments;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stayhome.HomeMapActivity;
import com.example.stayhome.R;
import com.example.stayhome.adpaters.OpenAllShopListAdapter;
import com.example.stayhome.data.ShopData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Queue;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeOpen.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeOpen} factory method to
 * create an instance of this fragment.
 */
public class HomeOpen extends Fragment {

    private ArrayList<ShopData> shopData = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final String TAG = "HOMEOPEN FRAGMENT";
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private boolean locationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Context mContext;
    private Location deviceLoc = new Location("");
    private double distance;
    private View rootView;
    private RecyclerView recyclerView;
    private OpenAllShopListAdapter adapter;
    private TextView noData;
    private TextView title;
    private ShopData shop;
    private String genreString;
    private EditText genreView;
    private int itemChecked;
    private String[] items = new String[] {"Banks", "Shops", "Hospitals", "Pharmacy", "Clinics", "Government Offices",
        "Private Companies", "Schools/Colleges", "Petrol/Diesel Pumps", "Home Businesses", "Industries"};


    private OnFragmentInteractionListener mListener;

    public HomeOpen() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_home_open_all, container, false);
        title = rootView.findViewById(R.id.nearby_shop_txt);
        recyclerView = rootView.findViewById(R.id.open_shop_list);
        TextView showMap = rootView.findViewById(R.id.view_in_map);
        noData = rootView.findViewById(R.id.no_data_view);
        genreView = rootView.findViewById(R.id.genre_type);
        genreString = genreView.getText().toString().toLowerCase();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OpenAllShopListAdapter(getContext(), shopData);
        recyclerView.setAdapter(adapter);

        genreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), HomeMapActivity.class);
                intent.putExtra("genre", genreString);
                startActivity(intent);
            }
        });

        return rootView;
    }
    private void showDialog(){
        Arrays.sort(items);
        itemChecked = Arrays.asList(items).indexOf(genreString);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getActivity())
                .setTitle("Select Genre")
                .setSingleChoiceItems(items, itemChecked, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: item clicked. "+ items[which]);
                        itemChecked = which;
                    }
                }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        genreView.setText(items[itemChecked]);
                        genreString = items[itemChecked].toLowerCase();
                        if (shopData.size() > 0){
                            shopData.clear();
                        }
                        updateUI();
                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialogBuilder.show();
    }

    private void updateUI(){
        title.setText("Opened ");
        Query queryDocumentSnapshot = FirebaseFirestore.getInstance().collection("ShopData")
                .whereEqualTo("shopGenre", genreString);
        if (isNetworkAvailable()){
            ProgressBar progressBar = new ProgressBar(getContext());
            progressBar.setVisibility(View.VISIBLE);

            queryDocumentSnapshot.get().addOnSuccessListener(getActivity(), new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots.isEmpty()){
                        noData.setVisibility(View.VISIBLE);
                        noData.setText("Couldn't find any active "+ genreString);
                        Log.d(TAG, "onSuccess: No active " + genreString +" were found.");
                    }else {
                        noData.setVisibility(View.GONE);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                        Date date = new Date();
                        String currentDate = dateFormat.format(date);
                        for (QueryDocumentSnapshot data: queryDocumentSnapshots){
                            shop = data.toObject(ShopData.class);
                            Location loc = new Location("");
                            loc.setLatitude(Double.valueOf(shop.getLatLng().get(0)));
                            loc.setLongitude(Double.valueOf(shop.getLatLng().get(1)));
                            distance = deviceLoc.distanceTo(loc) / 1000;
                            Log.d(TAG, "onDataChange: Distance: "+ distance);
                            if (distance < 20){
                                String openTime = shop.getOpenTime();
                                String closeTime = shop.getCloseTime();

                                if (currentDate.compareTo(openTime) >= 0 && currentDate.compareTo(closeTime) <= 0){
                                    shop.setDistance(distance);
                                    shop.setActive(true);
                                    Log.d(TAG, "onDataChange: Distance: "+ distance);
                                    shopData.add(shop);
                                }
                            }
                        }
                        if (shopData.size() > 0){
                            adapter.setItems(shopData);
                            adapter.notifyDataSetChanged();
                        }else {
                            noData.setVisibility(View.VISIBLE);
                            noData.setText("Couldn't find any active "+ genreString);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Failed to retrieve data from firestore.");
                    Toast.makeText(mContext, "failed to connect.", Toast.LENGTH_SHORT).show();
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
        }else {
            Toast.makeText(mContext, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        NavigationView navigation = (NavigationView)getActivity().findViewById(R.id.nav_bar_home);
        Menu drawer_menu = navigation.getMenu();
        MenuItem menuItem;
        menuItem = drawer_menu.findItem(R.id.nav_bar_home_frag);
        if(!menuItem.isChecked())
        {
            menuItem.setChecked(true);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: Getting location permissions.");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(mContext, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(mContext, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationPermissionGranted = true;
            }else{
                ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation(){
        Log.d(TAG, "getDeviceLocation: getting current device location");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        try {
            if (locationPermissionGranted){
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null){
                                deviceLoc.setLatitude(currentLocation.getLatitude());
                                deviceLoc.setLongitude(currentLocation.getLongitude());
                            }else {
                                getLocationPermission();
                            }

                        }else{
                            Log.d(TAG, "onComplete: Current location is null");
                            Log.d(TAG, "onComplete: Current location is null");
                        }
                    }
                });
            }
        }catch(SecurityException e ){
            Log.e(TAG, "getDeviceLocation: SecurityException: "+ e.getMessage() );
        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;

    }

    @Override
    public void onStart() {
        super.onStart();
        getLocationPermission();
        getCurrentLocation();
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
