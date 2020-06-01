package com.meroapp.stayhome;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.meroapp.stayhome.data.ShopData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class HomeMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    
    public static final String TAG = "HOME MAP ACTIVITY";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean locationPermissionGranted = false;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;
    private ShopData shop;

    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLat, currentLng;

    private float distance;
    private Location deviceLoc = new Location("");
    private ImageView gpsView;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_map);
        initMap();
        gpsView = findViewById(R.id.home_gps);
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
        query = FirebaseFirestore.getInstance().collection("ShopData").whereEqualTo("shopGenre", getIntent().getStringExtra("genre"));
        if (isNetworkAvailable()){
            ProgressBar progressBar = new ProgressBar(getApplicationContext());
            progressBar.setVisibility(View.VISIBLE);

            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots.isEmpty()){
                        Toast.makeText(HomeMapActivity.this, "No active " + getIntent().getStringExtra("genre") + " were found.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onSuccess: No active shops were found.");
                    }else {
                        for (QueryDocumentSnapshot data : queryDocumentSnapshots) {
                            shop = data.toObject(ShopData.class);
                            Location loc = new Location("");
                            loc.setLatitude(Double.valueOf(shop.getLatLng().get(0)));
                            loc.setLongitude(Double.valueOf(shop.getLatLng().get(1)));
                            distance = deviceLoc.distanceTo(loc) / 1000;
                            if (distance < 20) {
                                Log.d(TAG, "onDataChange: HOME MAP ACTIVITY. Distance from current location: " + distance);
                                showLocation(loc.getLatitude(), loc.getLongitude(), shop.getShopName());
                            }
                        }

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Failed to retrieve data from firestore.");
                    Toast.makeText(getApplicationContext(), "failed to connect.", Toast.LENGTH_SHORT).show();
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
        }else {
            Toast.makeText(this, "Please connect to internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLocation(Double lat, Double lng, String shopName){
        LatLng latLng = new LatLng(lat, lng);
        Log.d(TAG, "showLocation: print lat: "+ lat + " \nlng: "+ lng);
        gMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(shopName)).showInfoWindow();
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
                                Log.d(TAG, "onDataChange: Distance: "+ deviceLoc.getLatitude() +" " +deviceLoc.getLongitude());
                            }else {
                                Toast.makeText(HomeMapActivity.this, "Failed to find the location.", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Log.d(TAG, "onComplete: Current location is null");
                            Toast.makeText(HomeMapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.home_map);
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
