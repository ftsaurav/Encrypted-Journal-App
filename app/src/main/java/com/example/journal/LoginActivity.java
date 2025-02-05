package com.example.journal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import Util.JournalApi;

public class LoginActivity extends AppCompatActivity {
    private Button login_btn;
    private AutoCompleteTextView email_textview;
    private EditText password_textview;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private CollectionReference collectionReference= db.collection("Users");
    private TextView forget_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        login_btn= findViewById(R.id.login_login_btn);
        email_textview= findViewById(R.id.login_email);
        password_textview= findViewById(R.id.login_password);
        firebaseAuth= FirebaseAuth.getInstance();
        forget_password= findViewById(R.id.forget_password);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                String email= email_textview.getText().toString().trim();
                String password= password_textview.getText().toString();
                loginviaEmailPassword(email, password);

            }
        });

        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
            }
        });
    }
    private void loginviaEmailPassword(String email, String password) {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            ProgressBar progressBar = findViewById(R.id.login_progress);
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            if (currentUser != null) {
                                // Check if email is verified
                                if (!currentUser.isEmailVerified()) {
                                    // If the email is not verified, log the user out and show a message
                                    firebaseAuth.signOut();
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                                    return; // Exit the method to prevent further execution
                                }

                                // If email is verified, continue with the login process
                                String currentUserId = currentUser.getUid();

                                collectionReference.whereEqualTo("userId", currentUserId)
                                        .get()
                                        .addOnCompleteListener(queryTask -> {
                                            if (queryTask.isSuccessful() && queryTask.getResult() != null && !queryTask.getResult().isEmpty()) {
                                                String username = queryTask.getResult().getDocuments().get(0).getString("userName");
                                                if (username != null) {
                                                    JournalApi journalApi = JournalApi.getInstance();
                                                    journalApi.setUserId(currentUserId);
                                                    journalApi.setUsername(username);
                                                    progressBar.setVisibility(View.GONE);
                                                    startActivity(new Intent(LoginActivity.this, JournalListActivity.class));
                                                }
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(LoginActivity.this, "No user data found", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
        email_textview.setText("");
        password_textview.setText("");
    }


}