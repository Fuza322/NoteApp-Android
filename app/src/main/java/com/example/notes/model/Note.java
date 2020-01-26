package com.example.notes.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note
{
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "text")
    private String noteText;
    @ColumnInfo(name = "title")
    private String noteTitle;
    @ColumnInfo(name = "date")
    private long noteDate;
    @ColumnInfo(name = "noteTags")
    private String noteTags;

    @Ignore
    private boolean checked = false;

    public Note(String noteTitle, String noteText, String noteTags, long noteDate)
    {
        this.noteDate = noteDate;
        this.noteText = noteText;
        this.noteTitle = noteTitle;
        this.noteTags = noteTags;
    }

    public String getNoteText()
    {
        return noteText;
    }

    public void setNoteText(String noteText)
    {
        this.noteText = noteText;
    }

    public long getNoteDate()
    {
        return noteDate;
    }

    public void setNoteDate(long noteDate)
    {
        this.noteDate = noteDate;
    }

    public int getId()
    {
        return id;
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }

    public boolean isChecked()
    {
        return checked;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString()
    {
        return "Note{" +
                "id=" + id +
                ", noteDate=" + noteDate +
                '}';
    }

    public String getNoteTitle()
    {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle)
    {
        this.noteTitle = noteTitle;
    }

    public void setNoteTags(String tags)
    {
        this.noteTags = tags;
    }

    public String getNoteTags()
    {
        return noteTags;
    }
}
