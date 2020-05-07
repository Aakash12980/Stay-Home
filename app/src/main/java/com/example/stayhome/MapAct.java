package com.example.stayhome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapAct extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener{
    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker startMarker;
    private double currentLat, currentLng;
    private String currentAddress;
    private String name, genre, contact;
    private FirebaseUser user;
    private String newAddress, newLat, newLng;

    private boolean locationPermissionGranted = false;

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String TAG = "MapActivity";
    private AutocompleteSupportFragment autocompleteFragment;
    private AppCompatTextView addressInfo;
    private ImageView gpsView;
    private List<String > currentLatLng = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Button saveBtn = (Button) findViewById(R.id.select_loc_btn);
        Button cancelBtn = (Button) findViewById(R.id.cancel_loc_btn);
        addressInfo = findViewById(R.id.address_info);
        gpsView = (ImageView) findViewById(R.id.gps);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getIntent().hasExtra("frag")){
                    clearExtras();
                    Intent intent = new Intent(getApplicationContext(), CreateShop.class);
                    startActivity(intent);
                    finish();
                }else {
                    Bundle bundle = new Bundle();
                    bundle.putString("fragment", "setting");
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtras(bundle);
                    startActivity(i);
                    finish();
                }

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLatLng.add(String.valueOf(currentLat));
                currentLatLng.add(String.valueOf(currentLng));
                Map<String, Object > newData = new HashMap<>();
                newData.put("shopLoc", currentAddress);
                newData.put("latLng", currentLatLng);
                Log.d(TAG, "onClick: Current user location selected: "+ currentAddress);
                if (isNetworkAvailable()){
                    if (getIntent().hasExtra("frag")){
                        clearExtras();
                        Intent intent = new Intent(getApplicationContext(), CreateShop.class);
                        intent.putExtra("address", currentAddress);
                        intent.putExtra("lat", String.valueOf(currentLat));
                        intent.putExtra("lng", String.valueOf(currentLng));
                        startActivity(intent);
                        finish();
                    }else {
                        ProgressBar progressBar = new ProgressBar(getApplicationContext());
                        progressBar.setVisibility(View.VISIBLE);
                        FirebaseFirestore.getInstance().collection("ShopData").document(user.getUid())
                                .update(newData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: User location changed to :"+ currentAddress);
                                Bundle extras = new Bundle();
                                extras.putString("fragment", "setting");
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                i.putExtras(extras);
                                startActivity(i);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Failed to change location: "+ e.getMessage());
                                Toast.makeText(MapAct.this, "Failed to change location.", Toast.LENGTH_SHORT).show();
                                Bundle extras = new Bundle();
                                extras.putString("fragment", "setting");
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                i.putExtras(extras);
                                startActivity(i);
                                finish();
                            }
                        });
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                }else {
                    Toast.makeText(MapAct.this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Select location of your shop", Toast.LENGTH_SHORT).show();
        gMap = googleMap;
        if (locationPermissionGranted) {
            gMap.setMyLocationEnabled(true);
            gMap.getUiSettings().setMyLocationButtonEnabled(false);
            getCurrentLocation();
            init();
        }else {
            getLocationPermission();
            getCurrentLocation();
            Log.d(TAG, "onMapReady: Current address: "+ currentAddress);
        }
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setStartLocation(latLng.latitude, latLng.longitude, "");
            }
        });
    }
    private void init(){
        Log.d(TAG, "init: initializing ");
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.API_KEY));
        }
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                LatLng latLng = place.getLatLng();
                try {
                    setStartLocation(latLng.latitude, latLng.longitude, place.getAddress());
                    currentLat = latLng.latitude;
                    currentLng = latLng.longitude;
                    currentAddress = place.getAddress();
                }catch (NullPointerException e){
                    Toast.makeText(MapAct.this, "Please select another location.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        gpsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked gps icon");
                getCurrentLocation();
            }
        });
        hideSoftKeyboard();
    }

    private void setStartLocation(Double lat, Double lng, String addr){
        String address = "Current Address";
        if (addr.isEmpty()){
            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(lat, lng, 1);
                if (!addresses.isEmpty()){
                    address = addresses.get(0).getAddressLine(0);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }else {
            address = addr;
        }
        if (startMarker != null){
            startMarker.remove();
        }
        startMarker = gMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title("Select this location")
                .snippet("Near "+address));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM));
        addressInfo.setText(address);

        currentAddress = address;
        currentLat = lat;
        currentLng = lng;
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapAct.this);
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
                                setStartLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), "");
                            }else {
                                Toast.makeText(MapAct.this, "Failed to find the location.", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Log.d(TAG, "onComplete: Current location is null");
                            Toast.makeText(MapAct.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch(SecurityException e ){
            Log.e(TAG, "getDeviceLocation: SecurityException: "+ e.getMessage() );
        }

    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: Getting location permissions.");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationPermissionGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Called.");
        locationPermissionGranted = false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:
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
                    if (!Places.isInitialized()) {
                        Places.initialize(getApplicationContext(), getResources().getString(R.string.API_KEY));
                    }
                }
        }
    }


    private void clearExtras(){
        getIntent().removeExtra("frag");
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        getLocationPermission();
        initMap();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
