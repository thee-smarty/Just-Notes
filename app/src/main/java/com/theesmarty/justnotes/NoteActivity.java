package com.theesmarty.justnotes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// TODO: 5/26/24 Note auto save working remove the toasts its messy

public class NoteActivity extends AppCompatActivity {
    EditText title, content;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        title = findViewById(R.id.title);
        content = findViewById(R.id.content);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Add TextWatchers to save note on text change
        title.addTextChangedListener(noteTextWatcher);
        content.addTextChangedListener(noteTextWatcher);
    }

    private final TextWatcher noteTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No action needed before text change
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Save note when text changes
            saveNote();
        }

        @Override
        public void afterTextChanged(Editable s) {
            // No action needed after text change
        }
    };

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
            String noteId = new SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault()).format(new Date());
            Map<String, Object> note = new HashMap<>();
            note.put("title", noteTitle);
            note.put("content", noteContent);
            note.put("noteId", System.currentTimeMillis());

            firestore.collection("JustNotes").document(userId).collection("notes").document(noteId)
                    .set(note)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Note auto-saved!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to save note: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }
}
