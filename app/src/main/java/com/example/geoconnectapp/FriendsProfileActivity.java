package com.example.geoconnectapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.geoconnectapp.logic.Tracking;
import com.example.geoconnectapp.ui.fragments.LocationFragment;

public class FriendsProfileActivity extends AppCompatActivity {
    private ImageView arrowBack;
    private TextView distanceView;
    private AppCompatButton aloneBtn;
    private MainActivity parentActivity;
//    final double userLat[] = {parentActivity.getUserLat()};
//    final double userLong[] = {parentActivity.getUserLong()};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);

        // Register image view arrow back
        arrowBack = findViewById(R.id.arrowback);

        // Navigate back to the last page the user visit
        arrowBack.setOnClickListener(v -> finish());

//        // The text view that displays the distance from yourself to the geocache
//        distanceView = findViewById(R.id.profilePageDistance);
//
//        // Create an instance of MainActivity class
//        parentActivity = new MainActivity();
//
//        // Calling getTrackingHandler() method from MainActivity
//        Tracking trackingHandler = parentActivity.getTrackingHandler();
//
//        // Latitude and longitude of geocache
//        userLat[0] = parentActivity.getUserLat();
//        userLong[0] = parentActivity.getUserLong();
//
//        // Change the text view to the corresponding distance
//        distanceView.setText(String.format("%.2f m", trackingHandler.calculateDistance(userLat[0], userLong[0])));
//
        // Register find geocache alone button
        aloneBtn = findViewById(R.id.alone);

        // Navigate to Location fragment
        aloneBtn.setOnClickListener(v -> {
            // Obtain an instance of FragmentManager
            FragmentManager fragmentManager = getSupportFragmentManager();

            // Begin a new FragmentTransaction
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            LocationFragment locationFragment = new LocationFragment(); // Instantiate your fragment class
            fragmentTransaction.replace(R.id.frame_layout, locationFragment);

            // Commit the transaction
            fragmentTransaction.commit();
        });

    }
}