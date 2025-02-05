package com.example.journal;

import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Objects;

import Model.Journal;
import Util.JournalApi;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class PostJournalActivity extends AppCompatActivity {
    private ImageView imageView;
    private ImageView camera;
    private TextView username_textview;
    private TextView title_textview;
    private TextView notes_textview;
    private Button create_btn;
    private ProgressBar create_progress_bar;
    public static final int GALLERY_CODE= 1000;
   private Uri imageUri;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference= db.collection("Journal");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_journal);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.title_image_view);
        camera = findViewById(R.id.camera);
        username_textview= findViewById(R.id.username_textView);
        title_textview= findViewById(R.id.enter_title_textview);
        notes_textview= findViewById(R.id.notes_text_view);
        create_btn= findViewById(R.id.create_btn);
        create_progress_bar= findViewById(R.id.create_progress_bar);
        firebaseAuth= FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();


        JournalApi journalApi= JournalApi.getInstance();
        String username= journalApi.getUsername();
        String userId= journalApi.getUserId();

        if (username != null) {
            String url = "https://api.dicebear.com/5.x/initials/png?seed=" + username+"&size=1500";
            Picasso.get()
                    .load(url)
                    .transform(new CropCircleTransformation()) // Requires oval transformation for oval shape
                    .into(imageView);
            String upper_case_username= username.toUpperCase();
            username_textview.setText(String.format("HELLO %s!", upper_case_username));

        } else {
            Log.e("Loda", "Username is null!");
        }

        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser= firebaseAuth.getCurrentUser();
                if(currentUser!=null)
                {

                }
                else
                {

                }
            }
        };

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent= new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);

            }
        });

        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title= title_textview.getText().toString();
                String notes= notes_textview.getText().toString();
                create_progress_bar.setVisibility(View.VISIBLE);

                if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(notes) && imageUri!=null)
                {
                    StorageReference filepath= storageReference.child("journal_images")
                            .child("my_image_"+ Timestamp.now().getSeconds());

                    filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Journal journal= new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(notes);
                                    journal.setImageUrl(uri.toString());
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUserName(username);
                                    journal.setUserId(userId);

                                    collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            create_progress_bar.setVisibility(View.INVISIBLE);
                                            startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                            finish();
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(PostJournalActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                }
                            });
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    create_progress_bar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(PostJournalActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else
                {
                    create_progress_bar.setVisibility(View.INVISIBLE);
                    Toast.makeText(PostJournalActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE && resultCode==RESULT_OK)
        {
            if(data!=null)
            {
                Picasso.get().load(data.getData()).transform(new CropCircleTransformation()).into(imageView);
                imageUri= data.getData();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser= firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth!=null)
        {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
