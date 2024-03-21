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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geoconnectapp.FriendsProfileActivity;
import com.example.geoconnectapp.MainActivity;
import com.example.geoconnectapp.MainActivity.LocationCallback;
import com.example.geoconnectapp.MessageActivity;
import com.example.geoconnectapp.R;
import com.example.geoconnectapp.logic.Tracking;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirestoreKt;

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
    private FirebaseFirestore db;
    private Tracking trackingHandler;

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
        ImageView searchButton = view.findViewById(R.id.searchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendsProfileActivity.class);
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
                                double userLat = ((MainActivity) getActivity()).getUserLat();
                                double userLong = ((MainActivity) getActivity()).getUserLong();
                                distanceText.setText(((int)(100 * floor(trackingHandler.calculateDistance(userLat, userLong) * 0.01))) + "m");
                            } else {
                                Log.e("HomeFragment", "This should never happen");
                            }
                        };
                        ((MainActivity) getActivity()).getLocation(callback);
                    } else {
                        Log.d("HomeFragment", "No such document");
                    }
                } else {
                    Log.d("HomeFragment", "get failed with ", task.getException());
                }
            }
        });
    }
}