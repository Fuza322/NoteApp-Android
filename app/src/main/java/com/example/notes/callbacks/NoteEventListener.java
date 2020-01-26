package com.example.notes.callbacks;

import com.example.notes.model.Note;

public interface NoteEventListener
{
    void onNoteClick(Note note);
    void onNoteLongClick(Note note);
}
