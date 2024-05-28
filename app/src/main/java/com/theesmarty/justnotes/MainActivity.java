package com.theesmarty.justnotes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    Button add;
    ListView list;
    TextView info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        add = findViewById(R.id.add);
        list = findViewById(R.id.list);
        info = findViewById(R.id.info);

        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("829993235030-j3negle747muq1jcc40rpn8of65bt9vv.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,gso);
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user==null){
            signIn();
        }
        else {
            Toast.makeText(getApplicationContext(), "Login Success!\nWelcome "+user.getDisplayName(), Toast.LENGTH_SHORT).show();
            add.setEnabled(true);
        }

        add.setOnClickListener(view -> {
            Intent in = new Intent(MainActivity.this, NoteActivity.class);
            startActivity(in);
        });
        // TODO: 5/25/24 Retrieve the notes from database and display on screen

        // TODO: 5/28/24 when we click the note for update or access
        // Code to start NoteActivity for editing an existing note
        //Intent intent = new Intent(this, NoteActivity.class);
        //intent.putExtra("noteId", existingNoteId); // Pass the existing note ID
        //startActivity(intent);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                if (googleSignInAccount != null) {
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                    firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, task -> {
                        if (signInAccountTask.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login Success!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Log the error or display a more informative error message
                            Toast.makeText(getApplicationContext(), "Error Login!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (ApiException e) {
                // Log the error or display a more informative error message
                Toast.makeText(getApplicationContext(), "Sign In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }
}