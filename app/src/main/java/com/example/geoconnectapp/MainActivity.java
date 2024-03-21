package com.example.geoconnectapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.geoconnectapp.databinding.ActivityMainBinding;
import com.example.geoconnectapp.logic.SensorHandler;
import com.example.geoconnectapp.logic.Tracking;
import com.example.geoconnectapp.ui.fragments.FriendsFragment;
import com.example.geoconnectapp.ui.fragments.HomeFragment;
import com.example.geoconnectapp.ui.fragments.LocationFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private SensorHandler sensorHandler;
    private double userLat = Double.NaN;
    private double userLong = Double.NaN;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Tracking trackingHandler;
    private final String TAG = "MainActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        sensorHandler = new SensorHandler();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        getLocation(null);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().get("location") != null) {
                if ((boolean)getIntent().getExtras().get("location")) {
                    getGeocacheLocationAndTrack();
                    Toast.makeText(getApplicationContext(), "Finding geocache location...", Toast.LENGTH_SHORT).show();
                }
            }
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.bottom_home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.bottom_location) {
                replaceFragment(new LocationFragment());
            } else if (item.getItemId() == R.id.bottom_friend) {
                replaceFragment(new FriendsFragment());
            }

            return true;
        });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void updateGeocacheLocation(View v) {
        // Create a new user with a first and last name
        Map<String, Object> geocache = new HashMap<>();

        // Replace with location of geocache
        geocache.put("location", new GeoPoint(50.82119451701268, 6.009762502463593));
        geocache.put("username", "Quinnca77");

        // Add a new document with a generated ID
        db.collection("geocaches").document("0")
                .set(geocache)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Doc updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void getGeocacheLocationAndTrack() {
        DocumentReference docRef = db.collection("geocaches").document("0");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        trackingHandler = new Tracking(document.getData());
                        Toast.makeText(getApplicationContext(), "Geocache is being tracked now!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    trackingHandler = new Tracking(snapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    public void getLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Get users permission
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    1000);
        }

        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLat = location.getLatitude();
                        userLong = location.getLongitude();
                        Log.d("Location", "Location got! It's " + userLat + " " + userLong);
                        if (callback != null) {
                            callback.completedTask(true);
                        }
                    }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(sensorHandler, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(sensorHandler, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorHandler);
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public double updateOrientationAngles(View v) {
        double actualAngle = SensorHandler.updateOrientationAngles();
        Toast.makeText(this, String.valueOf(actualAngle), Toast.LENGTH_SHORT).show();
        return actualAngle;
    }

    // Displays angle on screen. 0 is north, 90 is east, 180 is south, 270 is west.
    public double calculateAngleDiff(View v) {
        if (!checkRobustness()) {
            return 0;
        } else {
            double angleDiff = trackingHandler.calculateAngleDiff(userLat, userLong);
            Toast.makeText(this, String.valueOf(angleDiff), Toast.LENGTH_SHORT).show();
            return angleDiff;
        }
    }

    public double calculateDistance(View v) {
        if (!checkRobustness()) {
            return 0;
        } else {
            double distance = trackingHandler.calculateDistance(userLat, userLong);
            Toast.makeText(this, String.valueOf(distance), Toast.LENGTH_SHORT).show();
            return distance;
        }
    }

    private boolean checkRobustness() {
        if (Double.isNaN(userLat)) {
            Toast.makeText(this, "Still fetching location!", Toast.LENGTH_LONG).show();
            return false;
        }
        if (trackingHandler == null) {
            Toast.makeText(this, "No geocache is being tracked!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public Tracking getTrackingHandler() {
        return trackingHandler;
    }

    public double getUserLat() {
        return userLat;
    }

    public double getUserLong() {
        return userLong;
    }

    public interface LocationCallback {
        void completedTask(boolean status);
    }
}