package com.example.journal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Objects;

import Model.Journal;
import Util.JournalApi;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class EditAcitivty extends AppCompatActivity {

    private ImageView imageView;
    private ImageView camera;
    private TextView usernameTextView;
    private TextView titleTextView;
    private TextView notesTextView;
    private Button updateButton;
    private ProgressBar progressBar;
    public static final int GALLERY_CODE = 1000;
    private Uri imageUri;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Journal");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_acitivty);

        // Initialize UI components
        imageView = findViewById(R.id.edit_title_image_view);
        camera = findViewById(R.id.edit_camera);
        usernameTextView = findViewById(R.id.edit_username_textView);
        titleTextView = findViewById(R.id.edit_enter_title_textview);
        notesTextView = findViewById(R.id.edit_notes_text_view);
        updateButton = findViewById(R.id.edit_update_btn);
        progressBar = findViewById(R.id.edit_create_progress_bar);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        JournalApi journalApi = JournalApi.getInstance();
        String username = journalApi.getUsername();
        String userId = journalApi.getUserId();

        // Retrieve Intent extras
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");
        String title = intent.getStringExtra("title");
        String notes = intent.getStringExtra("notes");
        String documentId = intent.getStringExtra("document_id");

        // Set username and avatar
        if (!TextUtils.isEmpty(username)) {
            String url = "https://api.dicebear.com/5.x/initials/png?seed=" + username + "&size=1500";
            Picasso.get().load(url).transform(new CropCircleTransformation()).into(imageView);
            usernameTextView.setText(String.format("HELLO %s!", username.toUpperCase()));
        } else {
            Log.e("EditActivity", "Username is null!");
        }

        // Set image if available
        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.get().load(imageUrl).transform(new CropCircleTransformation()).into(imageView);
        }

        titleTextView.setText(title != null ? title : "");
        notesTextView.setText(notes != null ? notes : "");



        // Set camera click listener
        camera.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_CODE);
        });

        // Set update button click listener
        updateButton.setOnClickListener(view -> updateJournal(
                titleTextView.getText().toString(),
                notesTextView.getText().toString(),
                documentId,
                userId,
                username));

        // Initialize auth state listener
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(EditAcitivty.this, LoginActivity.class));
                finish();
            }
        };
    }

    private void updateJournal(String title, String notes, String documentId, String userId, String username) {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(notes) || imageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        StorageReference filepath = storageReference.child("journal_images")
                .child("my_image_" + Timestamp.now().getSeconds());

        filepath.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                    collectionReference.document(documentId)
                            .update("title", title,
                                    "thought", notes,
                                    "imageUrl", uri.toString(),
                                    "timeAdded", new Timestamp(new Date()))
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(EditAcitivty.this, "Updated", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(EditAcitivty.this, JournalListActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.e("Firestore Error", e.getMessage());
                                Toast.makeText(EditAcitivty.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
        ).addOnFailureListener(e -> {
            progressBar.setVisibility(View.INVISIBLE);
            Log.e("Storage Error", Objects.requireNonNull(e.getMessage()));
            Toast.makeText(EditAcitivty.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).transform(new CropCircleTransformation()).into(imageView);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
