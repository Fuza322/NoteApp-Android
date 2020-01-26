package com.example.notes.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.notes.model.Note;

@Database(entities = Note.class, version = 1, exportSchema = false)
public abstract class NotesDB extends RoomDatabase
{
    public abstract NotesDao notesDao();
    private static NotesDB instance;
    private static final String DATABASE_NAME = "notesDb";

    public static NotesDB getInstance(Context context)
    {
        if(instance == null)
            instance = Room.databaseBuilder(context, NotesDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        return instance;
    }
}
