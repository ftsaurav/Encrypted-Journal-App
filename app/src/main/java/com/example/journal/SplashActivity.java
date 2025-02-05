package com.example.journal;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import Util.JournalApi;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private final FirebaseFirestore db= FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;
    private final CollectionReference collectionReference= db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_screen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PenWritingView penWritingView = findViewById(R.id.penWritingView);
        penWritingView.startAnimation(); // Start the pen animation when splash screen is shown



        firebaseAuth= FirebaseAuth.getInstance();
        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser= firebaseAuth.getCurrentUser();
                if(currentUser!=null)
                {
                    currentUser= firebaseAuth.getCurrentUser();
                    String currentUserId= currentUser.getUid();

                    collectionReference.whereEqualTo("userId", currentUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if(error==null)
                                    {
                                        assert value != null;
                                        if(!value.isEmpty())
                                        {
                                            for(QueryDocumentSnapshot snapshot: value)
                                            {
                                                String username= snapshot.getString("userName");
                                                JournalApi journalApi= JournalApi.getInstance();
                                                journalApi.setUsername(username);
                                                journalApi.setUserId(currentUserId);
                                                startActivity(new Intent(SplashActivity.this, JournalListActivity.class));
                                                finish();
                                            }
                                        }
                                    }

                                }
                            });
                }
                else
                {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }

            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser= firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth!=null)
        {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
