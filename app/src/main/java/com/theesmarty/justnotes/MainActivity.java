package com.theesmarty.justnotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private FirebaseFirestore firestore;
    private ArrayAdapter<String> noteAdapter;
    private List<String> noteList;
    private List<String> noteIds;

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

        noteList = new ArrayList<>();
        noteIds = new ArrayList<>();
        noteAdapter = new ArrayAdapter<>(this, R.layout.item_note, R.id.note_title, noteList);
        list.setAdapter(noteAdapter);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("YOUR_CLIENT_ID")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            signIn();
        } else {
            Toast.makeText(getApplicationContext(), "Login Success!\nWelcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
            add.setEnabled(true);
            loadNotes(user.getUid());
        }

        add.setOnClickListener(view -> {
            Intent in = new Intent(MainActivity.this, NoteActivity.class);
            startActivity(in);
        });

        list.setOnItemClickListener((parent, view, position, id) -> {
            String noteId = noteIds.get(position);
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            intent.putExtra("noteId", noteId);
            startActivity(intent);
        });

        // Long press listener to show delete option
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            showPopup(view, position);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null)
            loadNotes(user.getUid());
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
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login Success!", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                add.setEnabled(true);
                                loadNotes(user.getUid());
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Error Login!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Sign In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }

    private void loadNotes(String userId) {
        firestore.collection("JustNotes").document(userId).collection("notes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        noteList.clear();
                        noteIds.clear();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String noteTitle = document.getString("title");
                            String noteContent = document.getString("content");
                            String noteId = document.getId();
                            if (noteTitle == null || noteTitle.isEmpty()) {
                                if (noteContent != null && !noteContent.isEmpty()) {
                                    String date = dateFormat.format(new Date(document.getLong("noteId")));
                                    noteTitle = "No Title (" + date + ")";
                                } else {
                                    noteTitle = "No Title";
                                }
                            }
                            noteList.add(noteTitle);
                            noteIds.add(noteId);
                        }
                        noteAdapter.notifyDataSetChanged();

                        if (noteList.size() > 0) {
                            list.setVisibility(ListView.VISIBLE);
                            info.setVisibility(TextView.GONE);
                        } else {
                            list.setVisibility(ListView.GONE);
                            info.setVisibility(TextView.VISIBLE);
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Failed to load notes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPopup(View view, int position) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
            deleteNote(position);
            return true;
        });
        popup.show();
    }

    private void deleteNote(int position) {
        String noteId = noteIds.get(position);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            firestore.collection("JustNotes").document(userId).collection("notes").document(noteId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                        loadNotes(userId);
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to delete note: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
