package com.example.stayhome.fragments;

import android.app.TimePickerDialog;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private EditText name;
    private EditText loc;
    private TextView phone;
    private EditText genre;
    private TextView email;
//    private SwitchMaterial statusView;
    private EditText openTime, closeTime;
    private int mHour, mMin;
    private String openTimeInput, closeTimeInput;
    private String previousOpenTime, previousCloseTime;

    private FirebaseUser user;
    private ListenerRegistration listenerRegistration;
    private MaterialButton saveBtn;
    private MaterialButton cancelBtn;

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
//        statusView = rootView.findViewById(R.id.shop_switch);
        final MaterialButton editBtn = rootView.findViewById(R.id.edit_btn);
        email = rootView.findViewById(R.id.profile_email);
        openTime = rootView.findViewById(R.id.open_time_input);
        closeTime = rootView.findViewById(R.id.close_time_input);
        saveBtn = rootView.findViewById(R.id.time_save_btn);
        cancelBtn = rootView.findViewById(R.id.time_cancel_btn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (openTimeInput.compareTo(closeTimeInput) >= 0){
                    Toast.makeText(getContext(), "Invalid closing time", Toast.LENGTH_SHORT).show();
                    closeTime.setText(previousCloseTime);
                    return;
                }
                updateTime();
                saveBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTime.setText(previousOpenTime);
                closeTime.setText(previousCloseTime);
                saveBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new SettingFragment();
                getFragmentManager().beginTransaction().replace(R.id.nav_fragment_container, fragment, "setting_frag").addToBackStack("setting_frag").commit();

            }
        });

        openTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDialog(openTime);
            }
        });

        closeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDialog(closeTime);
            }
        });

//        statusView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isNetworkAvailable()){
//                    final boolean active;
//                    if (statusView.isChecked()){
//                        active = true;
//                    }else{
//                        active = false;
//                    }
//                    Map<String , Object> statusUpdate = new HashMap<>();
//                    statusUpdate.put("active", active);
//                    FirebaseFirestore.getInstance().collection("ShopData").document(user.getUid())
//                            .update(statusUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.d(TAG, "onSuccess: Shop status updtaed to :" + active);
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.d(TAG, "onFailure: Failed to change status.");
//                            Toast.makeText(getContext(), "Failed to change status.", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }else {
//                    Toast.makeText(v.getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });


        return rootView;
    }

    private void updateTime(){
        DocumentReference documentReference = FirebaseFirestore.getInstance()
                .collection("ShopData").document(user.getUid());

        if (isNetworkAvailable()){
            Map<String , Object> update = new HashMap<>();
            update.put("openTime", openTimeInput);
            update.put("closeTime", closeTimeInput);
            documentReference.update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: New value set for open Time and close Time.");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Failed to change the value for open Time or close Time.");
                    Toast.makeText(getActivity(), "Failed to change the value.", Toast.LENGTH_SHORT).show();
                }
            });

        }else {
            Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTimeDialog(final EditText timeView){
        final Calendar calendar = Calendar.getInstance();
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMin = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat mSDF = new SimpleDateFormat("HH:mm");
                String time = mSDF.format(calendar.getTime());
                timeView.setText(time);
                openTimeInput = openTime.getText().toString();
                closeTimeInput = closeTime.getText().toString();
                if (!openTimeInput.equals(previousOpenTime) || !closeTimeInput.equals(previousCloseTime)){
                    saveBtn.setVisibility(View.VISIBLE);
                    cancelBtn.setVisibility(View.VISIBLE);
                }
            }
        }, mHour, mMin, true);
        timePickerDialog.show();
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
//                        statusView.setChecked(shopData.isActive());
                        loc.setText(shopData.getShopLoc());
                        openTime.setText(shopData.getOpenTime());
                        closeTime.setText(shopData.getCloseTime());
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
    private void timeRevert(String openTime, String closeTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat saveDateFormat = new SimpleDateFormat("hh:mm a");
        try {
            Date date1 = simpleDateFormat.parse(openTime);
            previousOpenTime = saveDateFormat.format(date1);
        }catch (Exception e){
            Log.d(TAG, "timeConversion: Failed to parse time.");
        }
        try {
            Date date2 = simpleDateFormat.parse(closeTime);
            previousCloseTime = saveDateFormat.format(date2);
        }catch (Exception e){
            Log.d(TAG, "timeConversion: failed to parse time.");
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
