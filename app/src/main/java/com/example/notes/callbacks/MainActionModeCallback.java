package com.example.notes.callbacks;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.example.notes.R;

public abstract class MainActionModeCallback implements ActionMode.Callback
 {
    private ActionMode action;
    private MenuItem countItem;

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
    {
        actionMode.getMenuInflater().inflate(R.menu.main_action_mode, menu);
        this.action = actionMode;
        this.countItem = menu.findItem(R.id.action_checked_count);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu)
    {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode)
    {

    }

    public void setCount(String chackedCount)
    {
        if (countItem != null)
            this.countItem.setTitle(chackedCount);
    }

    public ActionMode getAction()
    {
        return action;
    }
}