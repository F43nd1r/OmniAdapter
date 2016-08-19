package com.faendir.omniadapter;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.faendir.omniadapter.model.Component;

import java.util.List;

/**
 * Created on 10.08.2016.
 *
 * @author F43nd1r
 */

public class ActionModeHelper<T extends Component> implements OmniAdapter.SelectionListener<T> {

    private final AppCompatActivity activity;
    private final ActionMode.Callback callback;
    private ActionMode actionMode;

    public ActionModeHelper(AppCompatActivity activity, ActionMode.Callback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    @Override
    public void onSelectionChanged(List<T> selected) {
        if (actionMode == null) {
            activity.startSupportActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    actionMode = mode;
                    return callback.onCreateActionMode(mode, menu);
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return callback.onPrepareActionMode(mode, menu);
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return callback.onActionItemClicked(mode, item);
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    callback.onDestroyActionMode(mode);
                    actionMode = null;
                }
            });
        } else {
            actionMode.invalidate();
        }
    }

    @Override
    public void onSelectionCleared() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }
}
