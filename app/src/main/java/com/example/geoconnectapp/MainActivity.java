package com.example.geoconnectapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoconnectapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private Button getLocationBtn;

    final int locationRequestCode = 1000;

    private double userLat = 0.0, userLong = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_friends, R.id.nav_map)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Instantiating from our fused location services provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // new Button
        getLocationBtn = findViewById(R.id.locationBtn);

        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getLocation();
            }
        });
    }

    public void handleText(View v) {
        String email = ((EditText) findViewById(R.id.emailField)).getText().toString();
        Log.d("email info", email);
        String password = ((EditText) findViewById(R.id.passwordField)).getText().toString();
        Log.d("password info", password);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("user creation", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            View text = findViewById(R.id.text_home);
                            ((TextView) text).setText("User created successfully!");;
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("user creation", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            View text = findViewById(R.id.text_home);
                            ((TextView) text).setText("User creation failed!");
                        }
                    }
                });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Get users permission
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        }
        else {
            // Get user current location
            this.fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            userLat = location.getLatitude();
                            userLong = location.getLongitude();
                            Toast.makeText(this, String.format(Locale.US, "%s %s", userLat, userLong), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Get user location
                    this.getLocation();
                }
                else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            //TODO: user is signed in
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}