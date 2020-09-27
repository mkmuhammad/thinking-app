package com.devar.self;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devar.self.util.PostAPI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = "CreateAccountActivity";
    private ProgressBar create_ProgressBar;
    private AutoCompleteTextView create_emailEditText;
    private EditText create_UsernameEditText, create_passwordEditText;
    private Button create_createButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initViews();
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = (FirebaseAuth firebaseAuth) -> {

            currentUser = firebaseAuth.getCurrentUser();

            if (currentUser != null) {
                Toast.makeText(this, "user created, please log in", Toast.LENGTH_SHORT).show();
            } else {
            }

            Objects.requireNonNull(getSupportActionBar())
                    .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startGColor)));
            getSupportActionBar().setElevation(0);
        };

        create_createButton.setOnClickListener(v -> {

            if (!isViewsEmpty()) {
                String email = create_emailEditText.getText().toString().trim();
                String password = create_passwordEditText.getText().toString().trim();
                String username = create_UsernameEditText.getText().toString().trim();

                createUserEmailAccount(email, password, username);

            } else {
                Toast.makeText(CreateAccountActivity.this, "Empty Fields Not Allowed", Toast.LENGTH_SHORT).show();
            }

        });

    }


    private boolean isStringsEmpty(String s1, String s2, String s3) {
        return (TextUtils.isEmpty(s1)) && (TextUtils.isEmpty(s2)) && (TextUtils.isEmpty(s3));
    }

    private boolean isViewsEmpty() {
        return (TextUtils.isEmpty(create_UsernameEditText.getText().toString()))
                && (TextUtils.isEmpty(create_emailEditText.getText().toString()))
                && (TextUtils.isEmpty(create_passwordEditText.getText().toString()));
    }

    private void initViews() {
        create_ProgressBar = findViewById(R.id.create_progress);
        create_emailEditText = findViewById(R.id.create_email);
        create_UsernameEditText = findViewById(R.id.create_username);
        create_passwordEditText = findViewById(R.id.create_password);
        create_createButton = findViewById(R.id.create_createAcc_button);
    }

    private void createUserEmailAccount(String email, String password, String username) {
        if (!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)&&!TextUtils.isEmpty(username)) {

            create_ProgressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener((Task<AuthResult> task) -> {
                        if (task.isSuccessful()) {
                            currentUser = firebaseAuth.getCurrentUser();
                            assert currentUser != null;
                            String currentUserId = currentUser.getUid();

                            Map<String, String> userObject = new HashMap<>();
                            userObject.put("userId", currentUserId);
                            userObject.put("username", username);

                            collectionReference.add(userObject)
                                    .addOnSuccessListener((DocumentReference documentReference) -> {

                                        documentReference.get().addOnCompleteListener((Task<DocumentSnapshot> task1) -> {

                                            if (Objects.requireNonNull(task1.getResult()).exists()) {

                                                create_ProgressBar.setVisibility(View.INVISIBLE);
                                                String name = task1.getResult().getString("username");

                                                PostAPI journalAPI = PostAPI.getInstance();
                                                journalAPI.setUserId(currentUserId);
                                                journalAPI.setUsername(name);


                                                startActivity(new Intent(CreateAccountActivity.this,LoginActivity.class));
                                                finish();
                                            } else {
                                                create_ProgressBar.setVisibility(View.INVISIBLE);
                                            }
                                        });

                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CreateAccountActivity.this, "Adding user failed, try again", Toast.LENGTH_SHORT).show();
                                        create_ProgressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "createUserEmailAccount: saving to Db Failed");
                                    });


                        } else {
                            Toast.makeText(CreateAccountActivity.this, "Server doesn't respond, try again", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener((Exception e) -> {
                        Log.d(TAG, "createUserEmailAccount: "+e.toString());
                        create_ProgressBar.setVisibility(View.INVISIBLE);

                    });
        } else {
            Toast.makeText(CreateAccountActivity.this,
                    "Empty Strings not Allowed", Toast.LENGTH_SHORT).show();
            create_ProgressBar.setVisibility(View.INVISIBLE);

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
        finish();
    }
}










