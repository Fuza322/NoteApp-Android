package com.example.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.notes.db.NotesDB;
import com.example.notes.db.NotesDao;
import com.example.notes.model.Note;

import java.util.Date;
import java.util.Objects;


public class EditNoteActivity extends AppCompatActivity
{

    private EditText inputNoteTitle;
    private EditText inputNoteText;
    private EditText inputNoteTags;

    private NotesDao dao;
    private Note temp;

    public static final String NOTE_EXTRA_Key = "note_id";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edite_note);

        Toolbar toolbar = findViewById(R.id.edit_note_activity_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        inputNoteTitle = findViewById(R.id.input_note_title);
        inputNoteText = findViewById(R.id.input_note_text);
        inputNoteTags = findViewById(R.id.input_note_tags);

        dao = NotesDB.getInstance(this).notesDao();
        if (getIntent().getExtras() != null)
        {
            int id = getIntent().getExtras().getInt(NOTE_EXTRA_Key, 0);
            temp = dao.getNoteById(id);
            inputNoteTitle.setText(temp.getNoteTitle());
            inputNoteText.setText(temp.getNoteText());
            inputNoteTags.setText(temp.getNoteTags());
        } else inputNoteTitle.setFocusable(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.edit_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        if (id==R.id.save_note)
            onSaveNote();
        return super.onOptionsItemSelected(item);
    }

    private void onSaveNote()
    {
        String title = inputNoteTitle.getText().toString();
        String text = inputNoteText.getText().toString();
        String tags = inputNoteTags.getText().toString();

        String[] arrayString = tags.split("");
        if(!arrayString[0].equals(""))
        {
            for(String tag: arrayString)
            {
                if(!tag.startsWith("#"))
                {
                    Toast.makeText(this, "Wrong tag!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        if (title.isEmpty())
            title = "Unnamed";
        long date = new Date().getTime();
        if (temp == null)
        {
            temp = new Note(title, text, tags, date);
            dao.insertNote(temp);
        } else {
            temp.setNoteText(text);
            temp.setNoteDate(date);
            temp.setNoteTitle(title);
            temp.setNoteTags(tags);
            dao.updateNote(temp);
        }
        finish();
    }
}
