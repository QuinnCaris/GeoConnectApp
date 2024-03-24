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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MessageActivity extends AppCompatActivity {

    FirebaseFirestore db;
    private LinearLayout linearLayoutMessages;
    private EditText editTextMessage;
    private TextView loadingText;

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
            db.collection("messages/Robin_messages/david")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                List<Map<String, Object>> messagesUser = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    Map<String, Object> docMap = doc.getData();
                                    messagesUser.add(docMap);
                                }
                                messagesUser.sort(new Comparator<Map<String, Object>>() {
                                    @Override
                                    public int compare(Map<String, Object> t0, Map<String, Object> t1) {
                                        return Long.compare((long)t0.get("rank"), (long)t1.get("rank"));
                                    }
                                });
                                for (Map<String, Object> message : messagesUser) {
                                    addMessageUserToLayout((String)message.get("text"));
                                }
                            } else {
                                Log.e("MessageActivity", "Couldn't retrieve messages");
                            }
                        }
                    });
        };

        this.loadingText = findViewById(R.id.loadingText);

        db.collection("messages/David_messages/robin")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<String> messages = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> documentMap = document.getData();

                                Log.d("MessageActivity", document.getId() + " => " + document.getData());
                                messages.add((String)documentMap.get("text"));
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
            db.collection("messages/Robin_messages/david")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<Map<String, Object>> messages = new ArrayList<>();
                            if (task.isSuccessful()) {
                                long highestRank = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> documentMap = document.getData();
                                    if ((long)documentMap.get("rank") > highestRank) {
                                        highestRank = (long)documentMap.get("rank");
                                    }
                                    Log.d("MessageActivity", document.getId() + " => " + document.getData());
                                }
                                addMessageToDatabase(message, highestRank);
                            } else {
                                Log.d("MessageActivity", "Error getting documents: ", task.getException());
                            }
                        }
                    });
            editTextMessage.setText(""); // Clear the editText
        }
    }

    private void addMessageUserToLayout(String message) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.linearLayoutMessages, new MessageUserFragment(message), "message_fragment_")
                .commit();
    }

    public void addMessageToDatabase(String message, long rank) {
        Map<String, Object> document = new HashMap<>();
        document.put("text", message);
        document.put("rank", rank + 1);

        db.collection("messages/Robin_messages/david")
                .add(document);
    }

    private void addMessageFriendToLayout(String message) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.linearLayoutMessages, new MessageFriendFragment(message), "message_fragment_")
                .commit();
    }

}
