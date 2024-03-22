package com.example.geoconnectapp;

import static java.lang.Math.floor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.geoconnectapp.logic.Tracking;
import com.example.geoconnectapp.ui.fragments.LocationFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FriendsProfileActivity extends AppCompatActivity {
    private ImageView arrowBack;
    private TextView distanceView;
    private AppCompatButton aloneBtn;
    private MainActivity parentActivity;
    private FirebaseFirestore db;
    private Tracking trackingHandler;
//    final double userLat[] = {parentActivity.getUserLat()};
//    final double userLong[] = {parentActivity.getUserLong()};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);

        this.db = FirebaseFirestore.getInstance();

        // Register image view arrow back
        arrowBack = findViewById(R.id.arrowback);

        // Navigate back to the last page the user visit
        arrowBack.setOnClickListener(v -> finish());

        // The text view that displays the distance from yourself to the geocache
        distanceView = findViewById(R.id.profilePageDistance);

        // Create an instance of MainActivity class
        parentActivity = new MainActivity();

        // Register find geocache alone button
        aloneBtn = findViewById(R.id.alone);

        // Navigate to Location fragment
        aloneBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FriendsProfileActivity.this, MainActivity.class);
            intent.putExtra("location", true);
            startActivity(intent);
        });

        distanceView.setText(getIntent().getStringExtra("distance"));
    }
}