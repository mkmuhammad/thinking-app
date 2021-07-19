package com.devar.self;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devar.self.model.PostObj;
import com.devar.self.util.PostAPI;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostJournalActivity";
    private static final int GALLERY_CODE = 1;
    private ImageView post_HeaderIV, post_CameraButtonIV;
    private TextView post_usernameTV, post_dateTV;
    private EditText post_titleET, post_descriptionET;
    private ProgressBar post_progressBar;
    private Button post_SaveButton;

    private String currentUserId;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("PostObj");

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        initViews();


        post_CameraButtonIV.setOnClickListener(this);
        post_SaveButton.setOnClickListener(this);

        authStateListener = (FirebaseAuth firebaseAuth) -> {
            user = firebaseAuth.getCurrentUser();

            if (user != null) {
                Log.d(TAG, "onCreate: authListener");
            } else {

            }
        };

        Objects.requireNonNull(getSupportActionBar())
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startGColor)));
        getSupportActionBar().setElevation(0);


    }

    private void initViews() {
        post_HeaderIV = findViewById(R.id.post_header);
        post_CameraButtonIV = findViewById(R.id.post_cameraButton);
        post_usernameTV = findViewById(R.id.post_usernameTextView);
        post_dateTV = findViewById(R.id.post_dateTextView);
        post_titleET = findViewById(R.id.post_titleEditText);
        post_descriptionET = findViewById(R.id.post_descriptionEditText);
        post_progressBar = findViewById(R.id.post_ProgressBar);
        post_SaveButton = findViewById(R.id.post_saveJournalButton);
        post_progressBar.setVisibility(View.INVISIBLE);

        if (PostAPI.getInstance() != null) {
            currentUserId = PostAPI.getInstance().getUserId();
            currentUserName = PostAPI.getInstance().getUsername();

            post_usernameTV.setText(currentUserName);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference =FirebaseStorage.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
        Log.d(TAG, "onStart: authListener");

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.post_cameraButton:
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
            case R.id.post_saveJournalButton:
                saveJournal();
                break;
        }
    }

    private boolean isStringsEmpty(String s1, String s2) {
        return (TextUtils.isEmpty(s1)) && (TextUtils.isEmpty(s2));
    }

    private void saveJournal() {
        String title = post_titleET.getText().toString().trim();
        String thoughts = post_descriptionET.getText().toString().trim();
        post_progressBar.setVisibility(View.VISIBLE);

        if (!isStringsEmpty(title, thoughts) && imageUri != null) {
            StorageReference filepath = storageReference
                    .child("journal_images")
                    .child("my_image_" + Timestamp.now().getSeconds());

            filepath.putFile(imageUri)
                    .addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> {

                        filepath.getDownloadUrl().addOnSuccessListener((Uri uri) -> {
                            String imageUrl = uri.toString();

                            PostObj postObj = new PostObj();
                            postObj.setTitle(title);
                            postObj.setThought(thoughts);
                            postObj.setImageUrl(imageUrl);
                            postObj.setTimeAdded(new Timestamp(new Date()));
                            postObj.setUsername(currentUserName);
                            postObj.setUserId(currentUserId);


                            collectionReference.add(postObj)
                                    .addOnSuccessListener((DocumentReference documentReference) -> {
                                        post_progressBar.setVisibility(View.INVISIBLE);
                                        startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "saveJournal: collection didn't added in fireStore " + e.toString());
                                        Toast.makeText(PostJournalActivity.this,
                                                "Post didn't upload, try again", Toast.LENGTH_SHORT).show();
                                        post_progressBar.setVisibility(View.INVISIBLE);

                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        post_progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(PostJournalActivity.this,
                                "Image didn't upload, try again", Toast.LENGTH_SHORT).show();

                    });
        } else{
            Toast.makeText(PostJournalActivity.this,
                    "Title, Thoughts and Image can not be empty", Toast.LENGTH_SHORT).show();
            post_progressBar.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            imageUri = data.getData();
            post_HeaderIV.setImageURI(imageUri);

            if (imageUri != null){
                post_CameraButtonIV.setAlpha(0.02f);
//                post_CameraButtonIV.setVisibility(View.INVISIBLE);
                post_usernameTV.setVisibility(View.INVISIBLE);

            }

        }
    }
}