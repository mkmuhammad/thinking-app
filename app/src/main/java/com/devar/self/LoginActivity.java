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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private Button login_LoginButton, login_CreateAccountButton;
    private AutoCompleteTextView login_EmailEditText;
    private EditText login_PasswordEditText;
    private ProgressBar login_ProgressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        Objects.requireNonNull(getSupportActionBar())
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startGColor)));
        getSupportActionBar().setElevation(0);


    }

    private void initViews() {
        login_LoginButton = findViewById(R.id.login_login_button);
        login_LoginButton.setOnClickListener(this);
        login_CreateAccountButton = findViewById(R.id.login_createAcc_button);
        login_CreateAccountButton.setOnClickListener(this);
        login_EmailEditText = findViewById(R.id.login_email);
        login_PasswordEditText = findViewById(R.id.login_password);
        login_ProgressBar = findViewById(R.id.login_progress);

        firebaseAuth = FirebaseAuth.getInstance();
    }

   /* private boolean isStringsEmpty(String s1, String s2) {
        return ((TextUtils.isEmpty(s1)) &&(TextUtils.isEmpty(s2)));
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_login_button:
                String email = login_EmailEditText.getText().toString().trim();
                String password = login_PasswordEditText.getText().toString().trim();
                login_ProgressBar.setVisibility(View.VISIBLE);
                loginEmailPasswordUser(email, password);
                break;
            case R.id.login_createAcc_button:
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
                break;
        }
    }

    private void loginEmailPasswordUser(String email, String password) {
        if (!TextUtils.isEmpty(email)&& !TextUtils.isEmpty(password)) {

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener((Task<AuthResult> task) -> {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            String currentUserId = user.getUid();
                            collectionReference
                                    .whereEqualTo("userId", currentUserId)
                                    .addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException error) -> {

                                        if (error != null) {//means we have a problem
                                            Toast.makeText(LoginActivity.this, "error from server occurred, tryAgain", Toast.LENGTH_SHORT).show();
                                        }
//                                        assert value != null;
                                        if (value != null) {
                                            if (!value.isEmpty()) {
                                                for (QueryDocumentSnapshot snapshot :
                                                        value) {

                                                    PostAPI postAPI = PostAPI.getInstance();
                                                    postAPI.setUsername(snapshot.getString("username"));
                                                    postAPI.setUserId(snapshot.getString("userId"));

                                                    login_ProgressBar.setVisibility(View.INVISIBLE);

                                                    startActivity(new Intent(LoginActivity.this, JournalListActivity.class));
                                                    finish();
                                                }

                                            }
                                        }else {
                                            startActivity(new Intent(LoginActivity.this,PostJournalActivity.class));
                                            finish();
                                        }

                                    });
                        }else {
                            login_ProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this,
                                    "User with given username or password don't exist", Toast.LENGTH_SHORT).show();
                        }

                    })
                    .addOnFailureListener((Exception e) -> {
                        login_ProgressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "loginEmailPasswordUser: "+e.getMessage());
                    });
        } else {
            login_ProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this, "Email and Password can not be empty", Toast.LENGTH_LONG).show();
        }
    }
}










