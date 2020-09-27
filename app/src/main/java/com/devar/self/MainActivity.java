package com.devar.self;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.devar.self.util.PostAPI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button getStartedButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUserFirebase();

        getStartedButton = findViewById(R.id.main_startButton);
        getStartedButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        Objects.requireNonNull(getSupportActionBar())
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startGColor)));
        getSupportActionBar().setElevation(0);


    }

    private void initUserFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    currentUser = firebaseAuth.getCurrentUser();
                    String currentUserId = currentUser.getUid();

                    collectionReference.whereEqualTo("userId", currentUserId)
                            .addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException error) -> {

                                if (error != null) {
                                    Log.d(TAG, "initUserFirebase: "+ error.toString());
                                }

                                assert value != null;
                                if (!value.isEmpty()) {

                                    for (QueryDocumentSnapshot snapshot :
                                            value) {
                                        PostAPI postAPI = PostAPI.getInstance();
                                        postAPI.setUserId(snapshot.getString("userId"));
                                        postAPI.setUsername(snapshot.getString("username"));

                                        MainActivity.this.startActivity(new Intent(MainActivity.this, JournalListActivity.class));
                                        MainActivity.this.finish();
                                    }
                                }

                            });

                } else {
                    Toast.makeText(MainActivity.this, "there is no user yet", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}