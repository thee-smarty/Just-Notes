package com.theesmarty.justnotes;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NoteActivity extends AppCompatActivity {
    EditText title, content;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String noteId; // Store noteId to keep track of the note

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        title = findViewById(R.id.title);
        content = findViewById(R.id.content);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Check if noteId is passed from the Intent
        if (getIntent().hasExtra("noteId")) {
            noteId = getIntent().getStringExtra("noteId");
            loadNote(); // Load existing note data
        } else {
            // Generate new noteId if this is a new note
            noteId = new SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault()).format(new Date());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save the note when the user leaves the activity
        saveNote();
    }

    private void loadNote() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference noteRef = firestore.collection("JustNotes").document(userId).collection("notes").document(noteId);

            noteRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    title.setText(documentSnapshot.getString("title"));
                    content.setText(documentSnapshot.getString("content"));
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveNote() {
        String noteTitle = title.getText().toString();
        String noteContent = content.getText().toString();

        if (TextUtils.isEmpty(noteTitle) && TextUtils.isEmpty(noteContent)) {
            // If both fields are empty, do not save
            return;
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> note = new HashMap<>();
            note.put("title", noteTitle);
            note.put("content", noteContent);
            note.put("noteId", System.currentTimeMillis());

            DocumentReference noteRef = firestore.collection("JustNotes").document(userId).collection("notes").document(noteId);

            noteRef.set(note)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to save note: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }
}
