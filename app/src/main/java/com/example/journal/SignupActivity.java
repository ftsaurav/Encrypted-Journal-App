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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

import Util.JournalApi;

public class SignupActivity extends AppCompatActivity {
    private Button signup_btn;
    private EditText username_textview;
    private AutoCompleteTextView email_textview;
    private EditText password_textview;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private final FirebaseFirestore db= FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference= db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signup_btn= findViewById(R.id.signup_signup_btn);
        username_textview= findViewById(R.id.signup_username);
        email_textview= findViewById(R.id.signup_email);
        password_textview= findViewById(R.id.signup_password);
        progressBar= findViewById(R.id.signup_progress);



        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                String username= username_textview.getText().toString().trim();
                String email= email_textview.getText().toString().trim();
                String password= password_textview.getText().toString();

                create_user_account(username, email, password);

            }
        });

        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser= firebaseAuth.getCurrentUser();
                if(currentUser!=null)
                {
//            user exists
                }
                else
                {
//              user not exists
                }
            }
        };
    }

    private void create_user_account(String username, String email, String password) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            progressBar.setVisibility(View.VISIBLE);

            username_textview.setText("");
            email_textview.setText("");
            password_textview.setText("");

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                String currentUserId = currentUser.getUid();

                                // Send email verification
                                currentUser.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Email verification sent

                                                    HashMap<String, String> userObj = new HashMap<>();
                                                    userObj.put("userId", currentUserId);
                                                    userObj.put("userName", username);

                                                    collectionReference.document(currentUserId).set(userObj)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    // Inform user to verify email before logging in
                                                                    Toast.makeText(SignupActivity.this, "Verification email sent. Please verify your email.", Toast.LENGTH_SHORT).show();
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                                    startActivity(intent); // Redirect user to login screen
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                } else {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(SignupActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                // Handle user creation failure
                                progressBar.setVisibility(View.INVISIBLE);
                                String error = Objects.requireNonNull(task.getException()).getMessage();
                                Toast.makeText(SignupActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }





    @Override
    protected void onStart() {
        super.onStart();

        currentUser= firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}