package com.example.stayhome;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.stayhome.fragments.CovidUpdateFragment;
import com.example.stayhome.fragments.HomeFragment;
import com.example.stayhome.fragments.MyShopFragment;
import com.example.stayhome.fragments.SettingFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    androidx.appcompat.widget.Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private Fragment fragment;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private boolean locationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;
    private View navHeader;

    private static final String TAG = "MAIN ACTIVITY";
    private FirebaseUser user;
    private DocumentReference documentReference;

    private TextView nameView;
    private TextView locView;
    private CircleImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationView = (NavigationView) findViewById(R.id.nav_bar_home);
        navHeader = navigationView.inflateHeaderView(R.layout.nav_header_view);


        if (savedInstanceState == null){
            if (getIntent().hasExtra("fragment")){
                String load_frag = getIntent().getStringExtra("fragment");
                getIntent().removeExtra("fragment");
                Log.d(TAG, "onCreate: Inside Intent: "+ load_frag);
                if (load_frag.equals("setting")){
                    fragment = new SettingFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_fragment_container, fragment, "setting_frag").addToBackStack("setting_frag").commit();
                }
            }else {
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_fragment_container, new HomeFragment(), "home_frag").addToBackStack("home_frag").commit();
            }
        }
        getSupportFragmentManager().popBackStack("home_frag", 0);

        toolbar = findViewById(R.id.nav_toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.background));
        setSupportActionBar(toolbar);

        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout = findViewById(R.id.nav_drawer);
        ActionBarDrawerToggle toggleDrawer = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggleDrawer);
        toggleDrawer.syncState();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_bar_home_frag:
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_fragment_container, new HomeFragment(), "home_frag").addToBackStack("home_frag").commit();
                break;
            case R.id.nav_bar_stats_frag:
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_fragment_container, new CovidUpdateFragment(), "stat_frag").addToBackStack("stat_frag").commit();
                break;
            case R.id.nav_bar_shop_frag:
                if (user.getDisplayName() != null && user.getDisplayName().length() >= 1){
                    Log.d(TAG, "onNavigationItemSelected: DISPLAY NAME: "+ user.getDisplayName());
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_fragment_container, new MyShopFragment(), "shop_frag").addToBackStack("shop_frag").commit();
                }else {
                    navigationView.getMenu().getItem(0).setChecked(true);
                    startActivity(new Intent(getApplicationContext(), CreateShop.class));
                }
                break;
            case R.id.nav_bar_setting_frag:
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_fragment_container, new SettingFragment(), "setting_frag").addToBackStack("setting_frag").commit();
                break;
            case R.id.nav_bar_login_frag:
                startActivity(new Intent(getApplicationContext(), LoginSignup.class));
                break;
            case R.id.nav_bar_logout_frag:
                FirebaseAuth.getInstance().signOut();
                navigationView.getMenu().getItem(0).setChecked(true);
                startActivity(new Intent(getApplicationContext(), LoginSignup.class));
                finishAffinity();
        }
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void updateUI(){
        if (user == null){
            hideItem();
        }else {
            unHideItem();
            final StorageReference storageReference = firebaseStorage.getReference("ProfilePic")
                    .child(user.getUid()).child("profile.jpg");
            documentReference = FirebaseFirestore.getInstance().collection("ShopInfo").document(user.getUid());
            Log.d(TAG, "updateUI: DISPLAY NAME: "+ user.getDisplayName());

            if (isNetworkAvailable()){
                DocumentReference documentReference = FirebaseFirestore.getInstance().collection("ShopData").document(user.getUid());
                documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()){
                            nameView = navHeader.findViewById(R.id.shop_prof_name);
                            locView = navHeader.findViewById(R.id.shop_prof_loc);
                            imgView = navHeader.findViewById(R.id.shop_prof_img);
                            Map<String, Object> shopData = documentSnapshot.getData();
                            locView.setText(shopData.get("shopLoc").toString());
                            nameView.setText(shopData.get("shopName").toString());

                            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()){
                                        Glide.with(getApplicationContext()).load(task.getResult()).error(R.drawable.pic).into(imgView);
                                    }else {
                                        Log.d(TAG, "onComplete: Failed to load image.");
                                    }
                                }
                            });
                        }else {
                            Log.d(TAG, "onEvent: No shop data available. ");
                        }
                    }
                });

            }

        }
    }

    private void hideItem()
    {
        Menu nav_Menu = (Menu) navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_bar_setting_frag).setVisible(false);
        nav_Menu.findItem(R.id.nav_bar_logout_frag).setVisible(false);
        nav_Menu.findItem(R.id.nav_bar_shop_frag).setVisible(false);
        navHeader.setVisibility(View.GONE);
    }
    private void unHideItem()
    {
        navigationView = (NavigationView) findViewById(R.id.nav_bar_home);
        Menu nav_Menu = (Menu) navigationView.getMenu();
        if (user.getDisplayName() == null || user.getDisplayName().length() < 1){
            nav_Menu.findItem(R.id.nav_bar_setting_frag).setVisible(false);
            navHeader.setVisibility(View.GONE);
        }
        if (user.getDisplayName() != null && user.getDisplayName().length() > 0){
            navHeader.setVisibility(View.VISIBLE);
            nameView = navHeader.findViewById(R.id.shop_prof_name);
            locView = navHeader.findViewById(R.id.shop_prof_loc);
            imgView = navHeader.findViewById(R.id.shop_prof_img);
        }else {
            navHeader.setVisibility(View.GONE);
        }

        nav_Menu.findItem(R.id.nav_bar_login_frag).setVisible(false);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        Fragment frag = getSupportFragmentManager().findFragmentByTag("home_frag");
        Fragment setFrag = getSupportFragmentManager().findFragmentByTag("setting_frag");
        if (setFrag != null){
            if ((getSupportFragmentManager().getBackStackEntryCount() == 1) && setFrag.isVisible()){
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_fragment_container, new HomeFragment()).commit();
            }
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else if ((frag != null && frag.isVisible()) || getSupportFragmentManager().getBackStackEntryCount() == 0){
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
            {
                finishAffinity();
                System.exit(0);
                return;
            }
            else { Toast.makeText(getBaseContext(), "Tap back button in order to exit", Toast.LENGTH_SHORT).show(); }

            mBackPressed = System.currentTimeMillis();
        }else {
            getSupportFragmentManager().popBackStack();

        }
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: Getting location permissions.");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationPermissionGranted = true;
                Log.d(TAG, "getLocationPermission: Location Permission granted");
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLocationPermission();
        user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "onStart: This is on start Callback.");
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
