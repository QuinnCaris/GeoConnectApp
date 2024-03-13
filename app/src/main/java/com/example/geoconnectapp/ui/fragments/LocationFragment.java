package com.example.geoconnectapp.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.geoconnectapp.MainActivity;
import com.example.geoconnectapp.R;
import com.example.geoconnectapp.logic.Tracking;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Handler handler = new Handler();
    private ImageView arrowView;

    public LocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocationFragment.
     */
    public static LocationFragment newInstance(String param1, String param2) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        startUpdatingArrow();
    }

    private void startUpdatingArrow() {
        MainActivity parentActivity = ((MainActivity)getActivity());
        Tracking trackingHandler = parentActivity.getTrackingHandler();
        double userLat = parentActivity.getUserLat();
        double userLong = parentActivity.getUserLong();
        Runnable updateArrowRunnable = new Runnable() {
            public void run() {
                // Update arrow rotation here
                arrowView.setRotation((float) trackingHandler.calculateAngleDiff(userLat, userLong) - 90);
                Log.d("Arrow runnable", "Updated arrow!");
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateArrowRunnable);
    }

    private void stopUpdatingArrow() {
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUpdatingArrow();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.arrowView = (ImageView) view.findViewById(R.id.arrow);
    }
}