package com.example.geoconnectapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geoconnectapp.dataModel.PreferenceManager;
import com.example.geoconnectapp.dataModel.User;
import com.example.geoconnectapp.databinding.ActivityLoginBinding;
import com.example.geoconnectapp.databinding.ActivityRegistrationPageBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RegistrationPage extends AppCompatActivity {
    private ActivityRegistrationPageBinding binding;
    private PreferenceManager preferenceManager;

    private static final String TAG = "PhoneAuthActivity";
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private TextView login;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhoneNum;
    private EditText editTextPW;
    private Button signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }

                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        binding = ActivityRegistrationPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        editTextName = findViewById(R.id.signInName);
        editTextEmail = findViewById(R.id.signInEmail);
        editTextPhoneNum = findViewById(R.id.signInUserPhoneNum);
        editTextPW = findViewById(R.id.signInPassword);
        signUpBtn = findViewById(R.id.signInBtn);

        // Add TextWatcher for email and password fields
        editTextEmail.addTextChangedListener(textWatcher);
        editTextPhoneNum.addTextChangedListener(textWatcher);
        editTextPW.addTextChangedListener(textWatcher);

        login = findViewById(R.id.login_text);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationPage.this, LoginActivity.class));
            }
        });
    }

    // TextWatcher to enable/disable the button based on the conditions
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Check conditions (e.g., email and password not empty)
            boolean conditionsFulfilled = !editTextName.getText().toString().isEmpty()
                    && !editTextEmail.getText().toString().isEmpty()
                    && !editTextPhoneNum.getText().toString().isEmpty()
                    && !editTextPW.getText().toString().isEmpty();

            // Enable or disable the button based on conditions
            signUpBtn.setEnabled(conditionsFulfilled);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void register() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(User.KEY_NAME, editTextName.getText().toString());
        user.put(User.KEY_EMAIL, editTextEmail.getText().toString());
        user.put(User.KEY_PASSWORD, editTextPW.getText().toString());
        database.collection(User.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putString(User.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(User.KEY_NAME, editTextName.getText().toString());
                    Log.d(TAG, "name registered");
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void signUp(View v) {
        Log.w(TAG, "signup button worked");
        Toast.makeText(RegistrationPage.this, "A verification code is sent!\nPlease check your sms inbox.",
                Toast.LENGTH_SHORT).show();
        TextView phoneNum = ((TextView) findViewById(R.id.signInUserPhoneNum));
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNum.getText().toString())       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();

                            linkToEmail();

                            Toast.makeText(RegistrationPage.this, "User made an account!",
                                    Toast.LENGTH_LONG).show();

                            register();

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // CAUTION: Somehow this also sometimes gets displayed when everything worked
                                //Toast.makeText(RegistrationPage.this, "Verification code was invalid",
                                //        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void linkToEmail() {
        String email = ((EditText) findViewById(R.id.signInEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.signInPassword)).getText().toString();
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            //FirebaseUser user = task.getResult().getUser();
                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            Toast.makeText(RegistrationPage.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void submitVerificationCode(View v) {
        Log.w(TAG, "verify works");
        TextView verification = ((TextView) findViewById(R.id.verificationField));
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verification.getText().toString());
        signInWithPhoneAuthCredential(credential);
    }
}