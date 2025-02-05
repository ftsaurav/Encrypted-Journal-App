package com.example.journal;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.forgot_password_email);
        resetPasswordButton = findViewById(R.id.forgot_password_btn);
        progressBar = findViewById(R.id.forgot_password_progress);

        resetPasswordButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(ForgetPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                resetPassword(email);
            }
        });
    }

    private void resetPassword(String email) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgetPasswordActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                        finish();  // Close the ForgotPasswordActivity or navigate back to login screen
                    } else {
                        Toast.makeText(ForgetPasswordActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
