package com.example.journal;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Model.ItemClickListener;
import Model.Journal;
import Util.JournalApi;
import ui.JournalRecyclerAdapter;

public class JournalListActivity extends AppCompatActivity implements ItemClickListener {

    private FloatingActionButton floatingActionButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journal");
    private List<Journal> journalList;
    private RecyclerView recyclerView;
    private JournalRecyclerAdapter adapter;
    private TextView noJournalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        // Customizing the status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.red));

        // Initializing UI components
        floatingActionButton = findViewById(R.id.floatingActionButton);
        recyclerView = findViewById(R.id.recycler_view);
        noJournalTextView = findViewById(R.id.journal_adding_hint);

        // Setting up RecyclerView
        journalList = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // FloatingActionButton click listener
        floatingActionButton.setOnClickListener(view -> {
            if (user != null) {
                startActivity(new Intent(JournalListActivity.this, PostJournalActivity.class));
            } else {
                Toast.makeText(JournalListActivity.this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            }
        });

        // Setting up toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Load journal entries
        fetchJournalEntries();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_signout) {
            signOutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOutUser() {
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
            startActivity(new Intent(JournalListActivity.this, MainActivity.class));
            finish();
        }
    }

    private void fetchJournalEntries() {
        if (JournalApi.getInstance().getUserId() == null) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        collectionReference.whereEqualTo("userId", JournalApi.getInstance().getUserId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        journalList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Journal journal = document.toObject(Journal.class);
                            journal.setDocument_id(document.getId());
                            journalList.add(journal);
                        }

                        adapter = new JournalRecyclerAdapter(JournalListActivity.this, journalList, this);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        noJournalTextView.setVisibility(View.GONE);
                    } else {
                        noJournalTextView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(JournalListActivity.this, "Failed to fetch journals: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(JournalListActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onEditClick(Journal journal, int position) {
        Intent intent= new Intent(JournalListActivity.this, EditAcitivty.class);
        intent.putExtra("imageUrl", journal.getImageUrl());
        intent.putExtra("title", journal.getTitle());
        intent.putExtra("notes", journal.getThought());
        intent.putExtra("document_id", journal.getDocument_id());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Journal journal, int position) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this journal?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    // Get the document reference using the journal's ID
                    collectionReference.document(journal.getDocument_id())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                journalList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error deleting item", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                .show();
    }

}
