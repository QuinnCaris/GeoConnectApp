package com.example.geoconnectapp;

import android.annotation.SuppressLint;
import android.os.Bundle;


import com.example.geoconnectapp.ui.fragments.FriendsFragment;
import com.example.geoconnectapp.ui.fragments.HomeFragment;
import com.example.geoconnectapp.ui.fragments.LocationFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geoconnectapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

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

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}