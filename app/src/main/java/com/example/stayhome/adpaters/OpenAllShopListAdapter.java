package com.example.stayhome.adpaters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stayhome.R;
import com.example.stayhome.data.ShopData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OpenAllShopListAdapter extends RecyclerView.Adapter<OpenAllShopListAdapter.OpenShopListViewHolder> {

    private ArrayList<ShopData> data = new ArrayList<>();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private Context context;
    private static final String TAG = "SHOP LIST ADAPTER";

    public OpenAllShopListAdapter(Context context, ArrayList<ShopData> data) {
        this.data.addAll(data);
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public OpenShopListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =LayoutInflater.from(parent.getContext());
        View view =inflater.inflate(R.layout.open_shop_list_view, parent, false);
        return new OpenShopListViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final OpenShopListViewHolder holder, int position) {
            try {
                holder.noData.setVisibility(View.INVISIBLE);
            }catch (NullPointerException e){
                Log.d(TAG, "onBindViewHolder: No Data view is Null.");
            }
            if (!data.isEmpty()){
                ShopData shop = data.get(position);
                holder.shopName.setText(shop.getShopName());
                holder.shopGenre.setText(shop.getShopGenre());
                holder.closeTime.setText(shop.getCloseTime());
                holder.openTime.setText(shop.getOpenTime());

                if (shop.isActive()){
                    holder.statusView.setVisibility(View.VISIBLE);
                }else {
                    holder.statusView.setVisibility(View.INVISIBLE);
                }

                StorageReference storageReference = firebaseStorage.getReference("ProfilePic")
                        .child(shop.getUid()).child("profile.jpg");

                storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Glide.with(context).load(task.getResult()).error(R.drawable.pic).into(holder.shopImg);

                        }
                    }
                });

                if (shop.getDistance() < 20){
                    holder.distView.setVisibility(View.VISIBLE);
                    holder.distView.setText(context.getString(R.string.mile, meterToMile(shop.getDistance())));
                }else {
                    holder.distView.setVisibility(View.INVISIBLE);
                }
                holder.locView.setText(shop.getShopLoc());

                if (shop.getContact() != null){
                    holder.contactView.setText(shop.getContact());
                }
            }
    }

    public class OpenShopListViewHolder extends RecyclerView.ViewHolder{
        ImageView shopImg;
        TextView shopName;
        TextView shopGenre;
        TextView statusView;
        TextView noData;
        TextView distView;
        TextView locView;
        TextView contactView;
        TextView openTime, closeTime;

        public OpenShopListViewHolder(@NonNull View itemView) {
            super(itemView);
            shopImg = itemView.findViewById(R.id.shop_img);
            shopName = itemView.findViewById(R.id.shop_name);
            shopGenre = itemView.findViewById(R.id.shop_genre);
            statusView = itemView.findViewById(R.id.shop_status);
            noData = itemView.findViewById(R.id.no_data_view);
            distView = itemView.findViewById(R.id.dist_view);
            locView = itemView.findViewById(R.id.shop_loc);
            contactView = itemView.findViewById(R.id.shop_contact);
            openTime = itemView.findViewById(R.id.shop_open_time);
            closeTime = itemView.findViewById(R.id.shop_close_time);

        }
    }

    public void setItems(ArrayList<ShopData> data){
        this.data = data;
    }

    public String meterToMile(double dist){
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        return decimalFormat.format(dist * 0.621371);
    }
}
