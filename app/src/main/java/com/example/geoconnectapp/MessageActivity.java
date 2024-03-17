package com.example.geoconnectapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MessageActivity extends AppCompatActivity {

    private LinearLayout linearLayoutMessages;
    private EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_message);



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
            addMessageToLayout(message);
            editTextMessage.setText(""); // Clear the editText
        }
    }

    private void addMessageToLayout(String message) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.linearLayoutMessages, new MessageUserFragment(), "message_fragment_")
                .commit();
    }

}
