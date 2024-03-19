package com.example.geoconnectapp.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.geoconnectapp.MainActivity;
import com.example.geoconnectapp.R;
import com.example.geoconnectapp.logic.Tracking;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private Handler handler = new Handler();
    private ImageView arrowView;
    private TextView distanceView;

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
        if (((MainActivity)getActivity()).getTrackingHandler() == null) {
            ((MainActivity)getActivity()).replaceFragment(new HomeFragment());
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
        startUpdatingUI();
    }

    private void startUpdatingUI() {
        MainActivity parentActivity = ((MainActivity)getActivity());
        Tracking trackingHandler = parentActivity.getTrackingHandler();
        final double[] userLat = {parentActivity.getUserLat()};
        final double[] userLong = {parentActivity.getUserLong()};
        Runnable updateArrowRunnable = new Runnable() {
            public void run() {
                userLat[0] = parentActivity.getUserLat();
                userLong[0] = parentActivity.getUserLong();
                arrowView.setRotation((float) trackingHandler.calculateAngleDiff(userLat[0], userLong[0]) - 90);
                Log.d("Arrow runnable", "Updated arrow!");
                handler.postDelayed(this, 1000);
            }
        };
        Runnable updateDistanceRunnable = new Runnable() {
            @SuppressLint("DefaultLocale")
            public void run() {
                parentActivity.getLocation(new View(parentActivity));
                userLat[0] = parentActivity.getUserLat();
                userLong[0] = parentActivity.getUserLong();
                distanceView.setText(String.format("%.2f m", trackingHandler.calculateDistance(userLat[0], userLong[0])));
                Log.d("Distance runnable", "Updated distance!");
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateArrowRunnable);
        handler.post(updateDistanceRunnable);
    }

    private void stopUpdatingUI() {
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUpdatingUI();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.arrowView = (ImageView) view.findViewById(R.id.arrow);
        this.distanceView = (TextView) view.findViewById(R.id.distanceView);
    }
}