package com.example.stayhome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.stayhome.data.ShopData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CreateShop extends AppCompatActivity {
    private String name;
    private String genre;
    private String loc;
    private String contact;
    private String lat, lng;
    private FirebaseUser user;
    TextInputLayout shopName;
    TextInputLayout shopGenre;
    TextInputLayout shopLoc;
    TextInputLayout shopContact;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ShopData shopData = new ShopData();
    private List<String> latlng = new ArrayList<>();
    private ProgressBar progressBar;
    public static final String TAG = "CREATE SHOP";
    private DocumentReference documentReference;
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shop);

        shopName = findViewById(R.id.create_shop_name);
        shopGenre = findViewById(R.id.create_genre);
        shopLoc = findViewById(R.id.create_location);
        shopContact = findViewById(R.id.create_contact);
        MaterialButton createBtn = findViewById(R.id.create_button);

        shopLoc.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapAct.class);
                intent.putExtra("name", name);
                intent.putExtra("genre", genre);
                intent.putExtra("contact", contact);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("address", loc);
                startActivity(intent);
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()){
                    createShop();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (name != null){
            shopName.getEditText().setText(name);
        }
        if (genre != null){
            shopGenre.getEditText().setText(genre);
        }
        if (loc != null){
            shopLoc.getEditText().setText(loc);
        }
        if (contact != null){
            shopContact.getEditText().setText(contact);
        }
        clearExtras();
    }

    private void createShop(){
        shopData.setShopName(name);
        shopData.setContact(contact);
        shopData.setShopGenre(genre);
        shopData.setLatLng(latlng);
        shopData.setShopLoc(loc);
        shopData.setUid(user.getUid());
        documentReference = firestore.collection("ShopData").document(user.getUid());
        if (isNetworkAvailable()){
            progressBar = new ProgressBar(getApplicationContext());
            progressBar.setVisibility(View.VISIBLE);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){
                        Toast.makeText(CreateShop.this, "You cannot create more than one shop account.", Toast.LENGTH_SHORT).show();

                    }else {
                        documentReference.set(shopData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (setDisplayName()){
                                    Log.d(TAG, "onSuccess: Shop account create for this user.");
                                    progressBar.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finishAffinity();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CreateShop.this, "Failed to create", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateShop.this, "Please", Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }
        progressBar.setVisibility(View.INVISIBLE);

    }
    private boolean setDisplayName(){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        if (isNetworkAvailable()){
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Display name is updated for "+ name);
                            }else {
                                flag = false;
                                documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(CreateShop.this, "Failed to create shop account. Please try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.d(TAG, "onComplete: Failed to set display name.");
                            }
                        }
                    });

        }else {
            flag = false;
            Toast.makeText(this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }
        return flag;
    }
    private boolean validateInputs(){
        try {
            name = shopName.getEditText().getText().toString().trim().toLowerCase();
        }catch (NullPointerException e){
            name = "";
        }
        try {
            genre = shopGenre.getEditText().getText().toString().trim().toLowerCase();
        }catch (NullPointerException e){
            genre ="";
        }
        try {
            loc = shopLoc.getEditText().getText().toString().trim().toLowerCase();
        }catch (NullPointerException e){
            loc = "";
        }
        try {
            contact = shopContact.getEditText().getText().toString().trim().toLowerCase();
        }catch (NullPointerException e){
            contact = "";
        }
        if (TextUtils.isEmpty(name)){
            shopName.setError("This field is required!");
            return false;
        }else if (TextUtils.isEmpty(genre)){
            shopGenre.setError("This field is required!");
            return false;
        }else if (TextUtils.isEmpty(loc)){
            shopLoc.setError("This field is required!");
            return false;
        }else if (TextUtils.isEmpty(contact)){
            shopContact.setError("This field is required.");
            return false;
        }
        return true;
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void getExtras(){
        name = getIntent().getStringExtra("name");
        genre = getIntent().getStringExtra("genre");
        contact = getIntent().getStringExtra("contact");
        loc = getIntent().getStringExtra("address");
        lat = getIntent().getStringExtra("lat");
        lng = getIntent().getStringExtra("lng");
        latlng.add(lat);
        latlng.add(lng);

    }
    private void clearExtras(){
        getIntent().removeExtra("name");
        getIntent().removeExtra("genre");
        getIntent().removeExtra("contact");
        getIntent().removeExtra("address");
        getIntent().removeExtra("lat");
        getIntent().removeExtra("lng");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().hasExtra("name")){
            getExtras();
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
    }
}
