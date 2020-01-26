package com.example.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.adapters.NotesAdapter;
import com.example.notes.callbacks.MainActionModeCallback;
import com.example.notes.callbacks.NoteEventListener;
import com.example.notes.db.NotesDB;
import com.example.notes.db.NotesDao;
import com.example.notes.model.Note;
import com.example.notes.utils.NoteUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.example.notes.EditNoteActivity.NOTE_EXTRA_Key;

public class MainActivity extends AppCompatActivity implements NoteEventListener
{
    private FloatingActionButton fab;
    private Menu menu;
    private ArrayList<Note> notes;
    private ArrayList<Note> savedNotes;
    private ArrayList<Note> base;
    //private ArrayList<Note> removeSortNotes;
    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private NotesDao dao;
    private int chackedCount = 0;
    private MainActionModeCallback actionModeCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        recyclerView = findViewById(R.id.notes_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        }
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onAddNewNote();
            }
        });
        dao = NotesDB.getInstance(this).notesDao();
    }

    private void loadNotes()
    {
        this.notes = new ArrayList<>();
        List<Note> list = dao.getNotes();
        base = new ArrayList<>(list);
        this.notes.addAll(list);
        this.adapter = new NotesAdapter(this, this.notes);
        this.adapter.setListener(this);
        this.recyclerView.setAdapter(adapter);
        showEmptyView();
        swipeToDeleteHelper.attachToRecyclerView(recyclerView);
    }

    private void showEmptyView()
    {
        if (notes.size() == 0)
        {
            this.recyclerView.setVisibility(View.GONE);
            findViewById(R.id.empty_notes_view).setVisibility(View.VISIBLE);

        } else {
            this.recyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.empty_notes_view).setVisibility(View.GONE);
        }
    }

    private void onAddNewNote()
    {
        startActivity(new Intent(this, EditNoteActivity.class));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        loadNotes();
    }

    @Override
    public void onNoteClick(Note note)
    {
        Intent edit = new Intent(this, EditNoteActivity.class);
        edit.putExtra(NOTE_EXTRA_Key, note.getId());
        startActivity(edit);
    }

    @Override
    public void onNoteLongClick(Note note)
    {
        note.setChecked(true);
        chackedCount = 1;
        adapter.setMultiCheckMode(true);
        adapter.setListener(new NoteEventListener()
        {
            @Override
            public void onNoteClick(Note note)
            {
                note.setChecked(!note.isChecked());
                if (note.isChecked())
                    chackedCount++;
                else chackedCount--;

                if (chackedCount == 0)
                {
                    actionModeCallback.getAction().finish();
                }

                actionModeCallback.setCount(chackedCount + "/" + notes.size());
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onNoteLongClick(Note note)
            {
            }
        });
        actionModeCallback = new MainActionModeCallback()
        {
            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) // метод переопределяющий кнопки при переходе между режимами
            {
                if (menuItem.getItemId() == R.id.action_delete_notes) //
                    onDeleteMultiNotes();
                actionMode.finish();
                return false;
            }
        };
        startActionMode(actionModeCallback);
        fab.hide();
        actionModeCallback.setCount(chackedCount + "/" + notes.size());
    }
    private void onDeleteMultiNotes()
    {
        List<Note> chackedNotes = adapter.getCheckedNotes();
        if (chackedNotes.size() != 0)
        {
            for (Note note : chackedNotes)
            {
                dao.deleteNote(note);
            }
            loadNotes();
            Toast.makeText(this, chackedNotes.size() + " Note(s) Delete successfully !", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, "No Note(s) selected", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onActionModeFinished(ActionMode mode)
    {
        super.onActionModeFinished(mode);
        adapter.setMultiCheckMode(false);
        adapter.setListener(this);
        fab.show();
    }
    private ItemTouchHelper swipeToDeleteHelper = new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
            {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView,
                                      @NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target)
                {
                    return false;
                }
                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
                {
                    if (notes != null)
                    {
                        Note swipedNote = notes.get(viewHolder.getAdapterPosition());
                        if (swipedNote != null)
                        {
                            swipeToDelete(swipedNote, viewHolder);
                        }

                    }
                }
            });

    private void swipeToDelete(final Note swipedNote, final RecyclerView.ViewHolder viewHolder)
    {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Delete Note?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) // кнопка удаления
                    {
                        dao.deleteNote(swipedNote);
                        notes.remove(swipedNote);
                        adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                        showEmptyView();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) // кнопка отмены удаления
                    {
                        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                })
                .setCancelable(false)
                .create().show();
    }
    public void removeSort () {
        loadNotes();
        adapter.notifyItemRangeChanged(0, notes.size());
    }


    public void sortNotes(int order, String attr)
    {
        Collections.sort(notes, new Sorter(order, attr));
        adapter.notifyItemRangeChanged(0, notes.size());
    }

    class Sorter implements Comparator<Note>
    {
        int order;
        String attr;
        Sorter(int order, String attr)
        {
            this.order = order;
            this.attr = attr;
        }

        public int compare(Note note1, Note note2)
        {
            if(attr.equals("date"))
            {
                if(note1.getNoteDate() == note2.getNoteDate()) return 0;
                else if (note1.getNoteDate() > note2.getNoteDate()) return order;
                else return -1*order;
            }
            else {
                if(note1.getNoteTitle().compareTo(note2.getNoteTitle()) == 0) return 0;
                else if(note1.getNoteTitle().compareTo(note2.getNoteTitle()) > 0) return order;
                else return -1*order;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText)
            {
                SearchByTag(newText);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                removeSort();
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener()
        {
            @Override
            public boolean onClose()
            {
                removeSort();
                adapter.notifyDataSetChanged();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        MenuItem menuItemTemp = menu.findItem(R.id.sort_title_in);
        menuItemTemp.setTitle(R.string.sort_by_title_up_inactive);

        menuItemTemp = menu.findItem(R.id.sort_title_dec);
        menuItemTemp.setTitle(R.string.sort_by_title_down_inactive);

        menuItemTemp = menu.findItem(R.id.sort_date_in);
        menuItemTemp.setTitle(R.string.sort_by_date_up_inactive);

        menuItemTemp = menu.findItem(R.id.sort_date_dec);
        menuItemTemp.setTitle(R.string.sort_by_date_down_inactive);

        menuItemTemp = menu.findItem(R.id.remove_sort);
        menuItemTemp.setTitle(R.string.sort_by_nothing_inactive);

        if (id == R.id.sort_title_in)
        {
            sortNotes(1, "title");
            MenuItem menuItem = menu.findItem(R.id.sort_title_in);
            menuItem.setTitle(R.string.sort_by_title_up_active);
        }
        else if (id == R.id.sort_title_dec)
        {
            sortNotes(-1, "title");
            MenuItem menuItem = menu.findItem(R.id.sort_title_dec);
            menuItem.setTitle(R.string.sort_by_title_down_active);
        }
        else if (id == R.id.sort_date_in)
        {
            sortNotes(1, "date");
            MenuItem menuItem = menu.findItem(R.id.sort_date_in);
            menuItem.setTitle(R.string.sort_by_date_up_active);
        }
        else if (id == R.id.sort_date_dec)
        {
            sortNotes(-1, "date");
            MenuItem menuItem = menu.findItem(R.id.sort_date_dec);
            menuItem.setTitle(R.string.sort_by_date_down_active);
        }
        else if (id == R.id.remove_sort)
        {
            removeSort();
            MenuItem menuItem = menu.findItem(R.id.remove_sort);
            menuItem.setTitle(R.string.sort_by_nothing_active);
        }

        return super.onOptionsItemSelected(item);
    }

    private void SearchByTag(String str) {
        ArrayList<Note> searchedNotes = new ArrayList<>();
        removeSort();
            for (Note n : notes) {
                if (n.getNoteTags().contains(str))
                    searchedNotes.add(n);
            }
        this.notes.clear();
        this.notes.addAll(searchedNotes);
        adapter.notifyDataSetChanged();
    }
}
