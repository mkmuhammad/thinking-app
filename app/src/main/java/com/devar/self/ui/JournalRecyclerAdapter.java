package com.devar.self.ui;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devar.self.R;
import com.devar.self.model.PostObj;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<PostObj> postObjList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("PostObj");

    public JournalRecyclerAdapter(Context context, List<PostObj> postObjList) {
        this.context = context;
        this.postObjList = postObjList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.journal_row, parent, false);
        return new ViewHolder(view, context);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostObj postObj = postObjList.get(position);
        String imageUrl;
        holder.row_titleTV.setText(postObj.getTitle());
        holder.row_thoughTV.setText(postObj.getThought());
        holder.row_name.setText(postObj.getUsername());
        imageUrl = postObj.getImageUrl();
        Picasso
                .get()
                .load(imageUrl)
                .placeholder(R.drawable.image_three)
                .fit()
                .into(holder.row_imageView);
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(postObj.getTimeAdded().getSeconds() * 1000);
        holder.row_dateTV.setText(timeAgo);

        holder.row_shareButton.setOnClickListener(v -> {
            collectionReference.whereEqualTo("imageUrl",postObj.getImageUrl())
                    .get()
                    .addOnSuccessListener((QuerySnapshot queryDocumentSnapshots) -> {
                        if (!queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot snapshot :
                                    queryDocumentSnapshots) {

                                PostObj snapshotObj = snapshot.toObject(PostObj.class);
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                String text =snapshotObj.getImageUrl();
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, text);
                                context.startActivity(Intent.createChooser(shareIntent, "Share post with"));
                            }

                        }else {
                            Toast.makeText(context, "no post exists", Toast.LENGTH_SHORT).show();
                        }


                    })
                    .addOnFailureListener(e -> {

                    });


        });
    }

    @Override
    public int getItemCount() {
        return postObjList.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout row_linearLayout;
        private TextView row_titleTV, row_thoughTV, row_dateTV, row_name;
        private ImageView row_imageView;
        private ImageButton row_shareButton;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;
            row_linearLayout = itemView.findViewById(R.id.row_linearLayout);
            row_titleTV = itemView.findViewById(R.id.row_title_list);
            row_thoughTV = itemView.findViewById(R.id.row_thoughts_list);
            row_dateTV = itemView.findViewById(R.id.row_timeStamp);
            row_imageView = itemView.findViewById(R.id.row_image_list);
            row_name = itemView.findViewById(R.id.row_userName);
            row_shareButton = itemView.findViewById(R.id.row_share_button);

        }
    }
}
