package com.devar.self;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devar.self.model.PostObj;
import com.devar.self.ui.JournalRecyclerAdapter;
import com.devar.self.util.PostAPI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JournalListActivity extends AppCompatActivity {

    private static final String TAG = "JournalListActivity";
    private List<PostObj> postObjList;
    private RecyclerView pList_recyclerView;
    private JournalRecyclerAdapter postRecyclerAdapter;
    private FloatingActionButton floatingActionButton;
    private TextView pList_noThought;
    private ImageButton shareButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("PostObj");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        initItems();

        Objects.requireNonNull(getSupportActionBar())
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startGColor)));
        getSupportActionBar().setElevation(0);

        floatingActionButton.setOnClickListener(v -> {
            startActivity(new Intent(JournalListActivity.this, PostJournalActivity.class));
        });

//        shareButton.setOnClickListener(v -> {
//            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            shareIntent.setType("text/plain");
//            String test = collectionReference.document("PostObj").getId();
//            String text =test;
//            shareIntent.putExtra(Intent.EXTRA_SUBJECT,text);
//            startActivity(Intent.createChooser(shareIntent,"Share post with"));
//        });

    }

    private void initItems() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        postObjList = new ArrayList<>();
        pList_noThought = findViewById(R.id.pList_no_thoughts);
        pList_recyclerView = findViewById(R.id.pList_recyclerView);
        pList_recyclerView.setHasFixedSize(true);
        pList_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        floatingActionButton = findViewById(R.id.floatingActionButton);
        shareButton = findViewById(R.id.row_share_button);

    }

    @Override
    protected void onStart() {
        super.onStart();
        collectionReference
                .whereEqualTo("userId", PostAPI.getInstance().getUserId())
                .get()
                .addOnSuccessListener((QuerySnapshot queryDocumentSnapshots) -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        pList_noThought.setVisibility(View.INVISIBLE);
                        for (QueryDocumentSnapshot postDocument :
                                queryDocumentSnapshots) {

                            PostObj postObj = postDocument.toObject(PostObj.class);

                            for (int i = 0; i < postObjList.size(); i++) {
                                if (postObjList.get(i).getImageUrl().equals(postObj.getImageUrl())){
                                    return;
                                }
                            }


                            postObjList.add(postObj);

                        }
                        postRecyclerAdapter = new JournalRecyclerAdapter(JournalListActivity.this, postObjList);
                        pList_recyclerView.setAdapter(postRecyclerAdapter);
                        postRecyclerAdapter.notifyDataSetChanged();

                    } else {
                        pList_noThought.setVisibility(View.VISIBLE);
                        Toast.makeText(JournalListActivity.this, "You have no post, add one", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JournalListActivity.this, "Network doesn't respond, try again!", Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(JournalListActivity.this, PostJournalActivity.class));
                }
                break;
            case R.id.action_signout:
                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    startActivity(new Intent(JournalListActivity.this, MainActivity.class));
                    finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}