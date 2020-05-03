package com.example.stayhome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    
    public static final String TAG = "HOME MAP ACTIVITY";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean locationPermissionGranted = false;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker startMarker;
    private double currentLat, currentLng;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private float distance;
    private Location deviceLoc = new Location("");
    private ImageView gpsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_map);

        gpsView = findViewById(R.id.home_gps);

        getLocationPermission();
        initMap();
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
        if (isNetworkAvailable()){
            databaseReference = FirebaseDatabase.getInstance().getReference("ShopInfo");
            valueEventListener = databaseReference.orderByChild("active").equalTo(true).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        for (DataSnapshot data: dataSnapshot.getChildren()
                        ) {
                            final ShopInfo shopInfo = data.getValue(ShopInfo.class);
                            Log.d(TAG, "onDataChange: Value of Shopinfo: " + shopInfo);
                            try {
                                Location loc = new Location("");
                                loc.setLatitude(Double.valueOf(shopInfo.getLatLng().get(0)));
                                loc.setLongitude(Double.valueOf(shopInfo.getLatLng().get(1)));
                                distance = deviceLoc.distanceTo(loc);
                                if (distance > 20000){
                                    Log.d(TAG, "onDataChange: Distance from current location: "+ distance);
                                    showLocation(loc.getLatitude(), loc.getLatitude(), shopInfo.getShopName());
                                    
                                }

                            }catch (NullPointerException e){
                                Log.d(TAG, "onDataChange: Null Object Referenced at the database.");
                            }
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else {
            Toast.makeText(this, "Please connect to internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLocation(Double lat, Double lng, String shopName){
        if (shopName.isEmpty()){
            currentLat = lat;
            currentLng = lng;
            deviceLoc.setLatitude(lat);
            deviceLoc.setLongitude(lng);
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM));
            
        }else {
            startMarker = gMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title(shopName));
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM));
        }
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
                                showLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), "");
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
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(valueEventListener);
        Log.d(TAG, "onDestroy: Database event listener destroyed successfully.");
    }
}
