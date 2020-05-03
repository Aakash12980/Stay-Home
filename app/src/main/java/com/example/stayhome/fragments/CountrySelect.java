package com.example.stayhome.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhome.R;
import com.example.stayhome.adpaters.CountrySelectAdapter;

public class CountrySelect extends AppCompatDialogFragment {
    private static final String TAG = "COUNTRYSELECT DIALOG";
    private CountrySelectAdapter adapter;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.country_select, null, false);
        RecyclerView recyclerView = view.findViewById(R.id.countryList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CountrySelectAdapter();
        recyclerView.setAdapter(adapter);

        builder.setView(view);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(),
                new RecyclerItemClickListener.OnItemClickListener(){
            @Override
            public void onItemClick(View view, int position) {
                Bundle bundle = new Bundle();
                TextView country = view.findViewById(R.id.country);
                Log.d(TAG, "onItemClick: Selected Item: "+ country.getText().toString());
                bundle.putString("newSelection", country.getText().toString());
                setArguments(bundle);
                if (getTargetFragment() != null){
                    Intent intent = CovidUpdateFragment.newIntent(country.getText().toString());
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                }

                dismiss();
            }
        }));

        return builder.create();

    }
}
