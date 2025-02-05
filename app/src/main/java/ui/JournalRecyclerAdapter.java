package ui;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journal.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import Model.ItemClickListener;
import Model.Journal;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<Journal> journalList;
    private ItemClickListener itemClickListener;

    public JournalRecyclerAdapter(Context context, List<Journal> journalList, ItemClickListener itemClickListener) {
        this.context = context;
        this.journalList = journalList;
        this.itemClickListener= itemClickListener;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.journal_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.ViewHolder holder, int position) {
        Journal journal = journalList.get(position);

        // Set title and thoughts
        String upper_case_title = journal.getTitle().toUpperCase();
        holder.title.setText(upper_case_title);
        holder.thoughts.setText(journal.getThought());

        // Format and set the date
        String timeAgo = DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds() * 1000).toString();
        holder.dateCreated.setText(timeAgo);
        holder.username.setText(journal.getUserName());

        holder.imageButton.setOnClickListener(view -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            String shareContent = "🌟 Here's a journal entry from " + journal.getUserName() + "! 🌟\n" +
                    "📅 Date: Created " + timeAgo + "\n" +
                    "📝 Title: \"" + journal.getTitle() + "\"\n" +
                    "💭 Thoughts: \"" + journal.getThought() + "\"\n" +
                    "Start journaling your thoughts today!";

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
            context.startActivity(shareIntent);
        });

//        holder.delete_btn.setOnClickListener(view -> {
//            // Show confirmation dialog
//            new AlertDialog.Builder(context)
//                    .setMessage("Are you sure you want to delete this item?")
//                    .setCancelable(false)
//                    .setPositiveButton("Yes", (dialog, id) -> {
//                        // Get the Firestore document reference to delete it from Firestore
//                        String journalId = journal.getId(); // Ensure your Journal object has an ID field
//                        FirebaseFirestore db = FirebaseFirestore.getInstance();
//                        db.collection("Journals") // Use the correct collection name
//                                .document(journalId)
//                                .delete()
//                                .addOnSuccessListener(aVoid -> {
//                                    // Remove item from the local list and update RecyclerView
//                                    journalList.remove(position);
//                                    notifyItemRemoved(position);
//                                    Toast.makeText(context, "Deleted from Firestore", Toast.LENGTH_SHORT).show();
//                                })
//                                .addOnFailureListener(e -> {
//                                    Toast.makeText(context, "Failed to delete from Firestore", Toast.LENGTH_SHORT).show();
//                                });
//                    })
//                    .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
//                    .show();
//        });


        holder.edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onEditClick(journal, holder.getAdapterPosition());
            }
        });

        holder.delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onDeleteClick(journal, holder.getAdapterPosition());
            }
        });

        // Load image with Picasso
        if (journal.getImageUrl() != null && !journal.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(journal.getImageUrl())
                    .fit()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.baseline_camera_alt_24);
        }
    }


    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, thoughts, dateCreated, username;
        public ImageView imageView;
        public ImageButton imageButton, edit_btn, delete_btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            title = itemView.findViewById(R.id.row_title);
            username = itemView.findViewById(R.id.row_username_textview);
            thoughts = itemView.findViewById(R.id.row_thoughts);
            dateCreated = itemView.findViewById(R.id.row_date_created);
            imageView = itemView.findViewById(R.id.row_image_view);
            imageButton = itemView.findViewById(R.id.row_share_btn);
            edit_btn = itemView.findViewById(R.id.row_edit_btn);
            delete_btn = itemView.findViewById(R.id.row_delete_btn);
        }
    }

    public interface OnItemClickListener {
        void onEditClick(ClipData.Item item);
        void onDeleteClick(ClipData.Item item, int position);
    }
}
