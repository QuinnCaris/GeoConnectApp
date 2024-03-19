package com.example.geoconnectapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geoconnectapp.ui.fragments.MessageFriendFragment;
import com.example.geoconnectapp.ui.fragments.MessageUserFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MessageActivity extends AppCompatActivity {

    FirebaseFirestore db;
    private LinearLayout linearLayoutMessages;
    private EditText editTextMessage;
    private TextView loadingText;

    // TODO: Add loading text
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_message);
        this.db = FirebaseFirestore.getInstance();

        Consumer<List<String>> callback = strings -> {
            this.loadingText.setVisibility(View.INVISIBLE);
            for (String message : strings) {
                addMessageFriendToLayout(message);
            }
        };

        this.loadingText = findViewById(R.id.loadingText);

        db.collection("messages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<String> messages = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> documentMap = document.getData();

                                Log.d("MessageActivity", document.getId() + " => " + document.getData());
                                messages.add((String)documentMap.get("message"));
                            }
                            callback.accept(messages);
                        } else {
                            Log.d("MessageActivity", "Error getting documents: ", task.getException());
                            callback.accept(messages);
                        }
                    }
                });


        linearLayoutMessages = findViewById(R.id.linearLayoutMessages);
        editTextMessage = findViewById(R.id.editTextMessage);

        Button buttonSend = findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            addMessageUserToLayout(message);
            editTextMessage.setText(""); // Clear the editText
        }
    }

    private void addMessageUserToLayout(String message) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.linearLayoutMessages, new MessageUserFragment(message), "message_fragment_")
                .commit();
    }

    private void addMessageFriendToLayout(String message) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.linearLayoutMessages, new MessageFriendFragment(message), "message_fragment_")
                .commit();
    }

}
