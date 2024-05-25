package com.theesmarty.justnotes;


import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class NoteActivity extends AppCompatActivity {
    EditText title,content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        title = findViewById(R.id.title);
        content = findViewById(R.id.content);

        // TODO: 5/25/24  Add functionality of adding the title and content together


    }
}