package com.example.stayhome.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.bumptech.glide.Glide;
import com.example.stayhome.R;
import com.example.stayhome.data.ShopData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyShopFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyShopFragment} factory method to
 * create an instance of this fragment.
 */
public class MyShopFragment extends Fragment {
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private static final String TAG = "MYSHOP Fragment";

    private OnFragmentInteractionListener mListener;
    private CircleImageView imgCard;
    private TextView name;
    private TextView loc;
    private TextView phone;
    private TextView genre;
    private TextView email;
    private SwitchMaterial statusView;

    private FirebaseUser user;
    private ListenerRegistration listenerRegistration;

    public MyShopFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_shop, container, false);
        imgCard = rootView.findViewById(R.id.profile_pic);
        name = rootView.findViewById(R.id.profile_name);
        loc = rootView.findViewById(R.id.profile_loc);
        phone = rootView.findViewById(R.id.profile_contact);
        genre = rootView.findViewById(R.id.profile_genre);
        statusView = rootView.findViewById(R.id.shop_switch);
        final MaterialButton editBtn = rootView.findViewById(R.id.edit_btn);
        email = rootView.findViewById(R.id.profile_email);




        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new SettingFragment();
                getFragmentManager().beginTransaction().replace(R.id.nav_fragment_container, fragment, "setting_frag").addToBackStack("setting_frag").commit();

            }
        });

        statusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()){
                    final boolean active;
                    if (statusView.isChecked()){
                        active = true;
                    }else{
                        active = false;
                    }
                    Map<String , Object> statusUpdate = new HashMap<>();
                    statusUpdate.put("active", active);
                    FirebaseFirestore.getInstance().collection("ShopData").document(user.getUid())
                            .update(statusUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: Shop status updtaed to :" + active);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Failed to change status.");
                            Toast.makeText(getContext(), "Failed to change status.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Toast.makeText(v.getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return rootView;
    }

    private void updateUI(){
        email.setText(user.getEmail());
        StorageReference storageReference = firebaseStorage.getReference("ProfilePic")
                .child(user.getUid()).child("profile.jpg");
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("ShopData").document(user.getUid());

        if (isNetworkAvailable()){
            ProgressBar progressBar = new ProgressBar(getContext());
            progressBar.setVisibility(View.VISIBLE);
            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Glide.with(getContext()).load(task.getResult()).error(R.drawable.pic).into(imgCard);
                    }
                }
            });

            listenerRegistration = documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot.exists()){
                        ShopData shopData = documentSnapshot.toObject(ShopData.class);
                        name.setText(shopData.getShopName());
                        genre.setText(shopData.getShopGenre());
                        phone.setText(shopData.getContact());
                        statusView.setChecked(shopData.isActive());
                        loc.setText(shopData.getShopLoc());
                    }else {
                        Log.d(TAG, "onEvent: User not found.");
                        Toast.makeText(getContext(), "Failed to get user information.", Toast.LENGTH_SHORT).show();
                    }
                    listenerRegistration.remove();
                }
            });
            progressBar.setVisibility(View.INVISIBLE);

        }else {
            Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
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
        menuItem = drawer_menu.findItem(R.id.nav_bar_shop_frag);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        updateUI();
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
