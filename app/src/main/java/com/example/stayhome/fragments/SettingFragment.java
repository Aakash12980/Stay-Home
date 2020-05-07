package com.example.stayhome.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.stayhome.MapAct;
import com.example.stayhome.R;
import com.example.stayhome.data.ShopData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingFragment} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {
    private static final int PICK_IMAGE = 1;
    private Uri imageUri;
    private TextView loc;
    private TextView name;
    private TextView genre;
    private TextView contact;
    ImageView image;
    private TextView email;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private static final String TAG = "SETTING FRAGMENT";
    private Context mContext;
    private DocumentReference documentReference;

    private OnFragmentInteractionListener mListener;
    private FirebaseUser user;
    private ProgressBar progressBar;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        final View nameView = rootView.findViewById(R.id.setting_name);
        View genreView = rootView.findViewById(R.id.setting_genre);
        View contactView = rootView.findViewById(R.id.setting_contact);
        View locView = rootView.findViewById(R.id.setting_location);
        final View imageView = rootView.findViewById(R.id.setting_pic_change);
        View emailView = rootView.findViewById(R.id.setting_email);
        View passwordView = rootView.findViewById(R.id.setting_password);

        loc = rootView.findViewById(R.id.setting_location_shop);
        name = rootView.findViewById(R.id.setting_name_shop);
        genre = rootView.findViewById(R.id.setting_genre_shop);
        contact = rootView.findViewById(R.id.setting_contact_shop);
        image = rootView.findViewById(R.id.setting_pic);
        email = rootView.findViewById(R.id.setting_email_shop);

        locView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), MapAct.class);
                startActivity(i);
            }
        });

        nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog("New Name");
            }
        });
        passwordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog("New Password");
            }
        });

        emailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog("New Email");
            }
        });
        genreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog("New Genre");
            }
        });
        contactView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog("New Contact");
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()){
                    selectImage();
                }else {
                    Toast.makeText(mContext, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private void updateUI(){
        email.setText(user.getEmail());
        StorageReference storageReference = firebaseStorage.getReference("ProfilePic")
                .child(user.getUid()).child("profile.jpg");
        documentReference = FirebaseFirestore.getInstance().collection("ShopData").document(user.getUid());
        if (isNetworkAvailable()){
            progressBar = new ProgressBar(mContext);
            progressBar.setVisibility(View.VISIBLE);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot.exists()){
                        ShopData shopData = documentSnapshot.toObject(ShopData.class);
                        name.setText(shopData.getShopName());
                        genre.setText(shopData.getShopGenre());
                        contact.setText(shopData.getContact());
                        loc.setText(shopData.getShopLoc());
                    }else {
                        Log.d(TAG, "onEvent: Document does not exists. ");
                        Toast.makeText(mContext, "Document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Glide.with(mContext).load(task.getResult()).error(R.drawable.pic).into(image);
                    }
                }
            });
            progressBar.setVisibility(View.INVISIBLE);

        }else {
            Toast.makeText(mContext, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        NavigationView navigation = (NavigationView)getActivity().findViewById(R.id.nav_bar_home);
        Menu drawer_menu = navigation.getMenu();
        MenuItem menuItem;
        menuItem = drawer_menu.findItem(R.id.nav_bar_setting_frag);
        if(!menuItem.isChecked())
        {
            menuItem.setChecked(true);
        }
    }
    private void openDialog(String tag){
        Bundle bundle = new Bundle();
        bundle.putString("frag", tag);
        UpdateField updateField = new UpdateField();
        updateField.setArguments(bundle);
        updateField.show(getChildFragmentManager(), tag);
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        updateUI();
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public void selectImage(){
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == -1 && data.getData() != null){
            imageUri = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                image.setImageBitmap(bitmap);
                uploadImage(bitmap);

            } catch (IOException e){
                e.printStackTrace();
                Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void uploadImage(Bitmap bitmap) {
        StorageReference storageReference = firebaseStorage.getReference("ProfilePic").child(user.getUid()).child("profile.jpg");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        UploadTask task = storageReference.putBytes(byteArrayOutputStream.toByteArray());
        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: Image uploaded successfully.");
                Toast.makeText(getContext(), "Profile image changed", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Failed to change profile image.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
