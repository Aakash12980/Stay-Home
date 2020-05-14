package com.example.stayhome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stayhome.data.MarkerLoader;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import java.util.List;

public class ScanActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "SCAN MAP ACTIVITY";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean locationPermissionGranted = false;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLat, currentLng;

    private float distance;
    private Location deviceLoc = new Location("");
    private ImageView gpsView;
    private TextView totalFound;
    private Double selectedDist = 4.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        totalFound = findViewById(R.id.scan_data_info);
        final Chip chip1 = findViewById(R.id.chip1);
        final Chip chip2 = findViewById(R.id.chip2);
        final Chip chip3 = findViewById(R.id.chip3);
        final Chip chip4 = findViewById(R.id.chip4);

        chip1.setChecked(true);
        chip1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDist = 4.0;
                updateUI();
            }
        });
        chip2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDist = 8.0;
                updateUI();
            }
        });
        chip3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDist = 16.0;
                updateUI();
            }
        });
        chip4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDist = 32.0;
                updateUI();
            }
        });

        initMap();
        gpsView = findViewById(R.id.scan_gps);
    }

    private void updateUI(){
        if (isNetworkAvailable()){
            try {
                new MarkerLoader(new MarkerLoader.AsyncDataListener() {
                    @Override
                    public void getResult(List<Double[]> covidData) {
                        if (covidData != null){
                            int count = 0;
                            Log.d(TAG, "getResult: Total result: "+ covidData.size());
                            Log.d(TAG, "getResult: Distance length: "+ selectedDist);
                            for (Double[] latLng: covidData
                                 ) {
                                Location loc = new Location("");
                                loc.setLatitude(latLng[0]);
                                loc.setLongitude(latLng[1]);
                                distance = deviceLoc.distanceTo(loc) / 1000;
                                if (distance <= selectedDist){
                                    count += 1;
                                    Log.d(TAG, "getResult: Count: "+ count);
                                    showLocation(loc.getLatitude(), loc.getLongitude());
                                }
                            }
                            Log.d(TAG, "getResult: Total count: " +count);
                            totalFound.setText(String.valueOf(count));
                        }
                    }
                }).execute();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this, "Failed to get the data.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if (locationPermissionGranted) {
            gMap.setMyLocationEnabled(true);
            gMap.getUiSettings().setMyLocationButtonEnabled(false);
            getCurrentLocation();
        }else {
            getLocationPermission();
            getCurrentLocation();
        }
        gpsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked gps icon");
                getCurrentLocation();
            }
        });
        updateUI();
    }

    private void showLocation(Double lat, Double lng){
        LatLng latLng = new LatLng(lat, lng);
        gMap.addMarker(new MarkerOptions()
                .position(latLng).title(""));
    }

    private void myLocation(Double lat, Double lng){
        currentLat = lat;
        currentLng = lng;
        deviceLoc.setLatitude(lat);
        deviceLoc.setLongitude(lng);
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM));
    }
    private void getCurrentLocation(){
        Log.d(TAG, "getDeviceLocation: getting current device location");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
                                myLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
                            }else {
                                Toast.makeText(ScanActivity.this, "Failed to find the location.", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Log.d(TAG, "onComplete: Current location is null");
                            Toast.makeText(ScanActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch(SecurityException e ){
            Log.e(TAG, "getDeviceLocation: SecurityException: "+ e.getMessage() );
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Called.");
        locationPermissionGranted = false;
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0){
                for (int i = 0; i < grantResults.length; i++){
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                        locationPermissionGranted = false;
                        Log.d(TAG, "onRequestPermissionsResult: permission failed!");
                        return;
                    }
                }
                Log.d(TAG, "onRequestPermissionsResult: Permission granted");
                locationPermissionGranted = true;
            }
        }
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: Getting location permissions.");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationPermissionGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void initMap(){
        Log.d(TAG, "initMap: initializing Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.scan_map);
        mapFragment.getMapAsync(this);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLocationPermission();
        initMap();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
