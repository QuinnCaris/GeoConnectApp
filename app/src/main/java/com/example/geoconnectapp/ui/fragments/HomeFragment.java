package com.example.geoconnectapp.ui.fragments;

import static java.lang.Math.floor;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geoconnectapp.FriendsProfileActivity;
import com.example.geoconnectapp.LoginActivity;
import com.example.geoconnectapp.MainActivity;
import com.example.geoconnectapp.MainActivity.LocationCallback;
import com.example.geoconnectapp.MessageActivity;
import com.example.geoconnectapp.R;
import com.example.geoconnectapp.dataModel.PreferenceManager;
import com.example.geoconnectapp.dataModel.User;
import com.example.geoconnectapp.logic.Tracking;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirestoreKt;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private TextView distanceText;
    private Button logOutButton;
    private FirebaseFirestore db;
    private Tracking trackingHandler;
    private FirebaseAuth mAuth;
    private TextView name;

    private PreferenceManager preferenceManager;

    public HomeFragment() {
        // Required empty public constructor
    }



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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

        this.db = FirebaseFirestore.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity parentActivity = ((MainActivity)getActivity());
        ImageView searchButton = view.findViewById(R.id.searchButton);
        logOutButton = view.findViewById(R.id.logOutButton);
        name = view.findViewById(R.id.name);
        mAuth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(requireContext());

        name.setText(preferenceManager.getString(User.KEY_NAME));
        getToken();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendsProfileActivity.class);
                intent.putExtra("distance", distanceText.getText().toString());
                startActivity(intent);
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                deleteToken();
                mAuth.signOut();
                Log.d("Log out", "Logging out");
                Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });


        this.distanceText = view.findViewById(R.id.distanceTextHome);
        distanceText.setText("Loading");

        DocumentReference docRef = db.collection("geocaches").document("0");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("HomeFragment", "DocumentSnapshot data: " + document.getData());
                        trackingHandler = new Tracking(document.getData());
                        LocationCallback callback = taskStatus -> {
                            if (taskStatus) {
                                if (isAdded()) {
                                    double userLat = ((MainActivity) getActivity()).getUserLat();
                                    double userLong = ((MainActivity) getActivity()).getUserLong();
                                    distanceText.setText(((int)(100 * floor(trackingHandler.calculateDistance(userLat, userLong) * 0.01))) + "m");
                                }
                            } else {
                                Log.e("HomeFragment", "This should never happen");
                            }
                        };
                        if (isAdded()) {
                            ((MainActivity) getActivity()).getLocation(callback);
                        }
                    } else {
                        Log.d("HomeFragment", "No such document");
                    }
                } else {
                    Log.d("HomeFragment", "get failed with ", task.getException());
                }
            }
        });
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        DocumentReference documentReference =
                db.collection(User.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(User.KEY_USER_ID)
                );
        documentReference.update(User.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> Log.d("Token", "Token Updated"))
                .addOnFailureListener(e -> Log.d("Token", "Unable to update token"));
    }

    private void deleteToken() {
        // Retrieve the user ID from PreferenceManager
        String userId = preferenceManager.getString(User.KEY_USER_ID);
        if (userId != null) {
            // User ID is not null, proceed with deleting the token
            DocumentReference documentReference =
                    db.collection(User.KEY_COLLECTION_USERS).document(userId);
            documentReference.update(User.KEY_FCM_TOKEN, FieldValue.delete())
                    .addOnSuccessListener(unused -> {
                        // Token deletion successful
                        preferenceManager.clear();
                        Log.d("Token", "Token deleted");
                        // Call signOut method after token deletion is successful
                        mAuth.signOut();
                        Log.d("Log out", "Logging out");
                        Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        // Token deletion failed
                        Log.e("Token", "Unable to delete token", e);
                    });
        } else {
            // User ID is null, unable to delete token
            Log.e("Token", "User ID is null, unable to delete token");
        }
    }
}