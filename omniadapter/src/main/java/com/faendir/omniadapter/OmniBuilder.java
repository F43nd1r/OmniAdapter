package com.faendir.omniadapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;

import com.faendir.omniadapter.model.Action;
import com.faendir.omniadapter.model.Component;
import com.faendir.omniadapter.model.DeepObservableList;
import com.faendir.omniadapter.model.SelectionMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 09.08.2016.
 *
 * @author F43nd1r
 */

public class OmniBuilder<T extends Component> {
    @NonNull
    private final Context context;
    @NonNull
    private final DeepObservableList<T> dataSource;
    @NonNull
    private final OmniAdapter.Controller<T> controller;
    @NonNull
    private Action.Click click;
    @NonNull
    private Action.LongClick longClick;
    @NonNull
    private Action.Swipe swipeToLeft;
    @NonNull
    private Action.Swipe swipeToRight;
    @NonNull
    private RecyclerView.LayoutManager layoutManager;
    @ColorInt
    private int highlightColor;
    @ColorInt
    private int selectionColor;
    @NonNull
    private SelectionMode selectionMode;
    private int expandUntilLevelOnStartup;
    private boolean deselectChildrenOnCollapse;
    @NonNull
    private final List<OmniAdapter.SelectionListener<T>> selectionListeners;
    @NonNull
    private final SparseArray<String> enabledUndoActions;
    @NonNull
    private String undoText;
    @NonNull
    private final List<OmniAdapter.UndoListener<T>> undoListeners;
    private int insetDpPerLevel;
    private boolean insetAsMargin;

    public OmniBuilder(@NonNull Context context, @NonNull DeepObservableList<T> dataSource, @NonNull OmniAdapter.Controller<T> controller) {
        this.context = context;
        this.dataSource = dataSource;
        this.controller = controller;
        click = new Action.Click(Action.NONE).setDefaultCompositeAction(Action.EXPAND);
        longClick = new Action.LongClick(Action.NONE);
        swipeToLeft = new Action.Swipe(Action.NONE);
        swipeToRight = new Action.Swipe(Action.NONE);
        layoutManager = new LinearLayoutManager(context);
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{R.attr.colorControlHighlight, R.attr.colorControlActivated});
        highlightColor = array.getColor(0, Color.TRANSPARENT);
        selectionColor = array.getColor(1, Color.TRANSPARENT);
        array.recycle();
        selectionMode = SelectionMode.MULTI;
        expandUntilLevelOnStartup = -1;
        deselectChildrenOnCollapse = true;
        selectionListeners = new ArrayList<>();
        enabledUndoActions = new SparseArray<>();
        undoText = "Undo";
        undoListeners = new ArrayList<>();
        insetDpPerLevel = 0;
        insetAsMargin = false;
    }

    public OmniBuilder<T> setClick(@NonNull Action.Click click) {
        this.click = click;
        return this;
    }

    public OmniBuilder<T> setLongClick(@NonNull Action.LongClick longClick) {
        this.longClick = longClick;
        return this;
    }

    public OmniBuilder<T> setSwipeToLeft(@NonNull Action.Swipe swipeToLeft) {
        this.swipeToLeft = swipeToLeft;
        return this;
    }

    public OmniBuilder<T> setSwipeToRight(@NonNull Action.Swipe swipeToRight) {
        this.swipeToRight = swipeToRight;
        return this;
    }

    public OmniBuilder<T> setLayoutManager(@NonNull RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        return this;
    }

    public OmniBuilder<T> setHighlightColor(@ColorInt int highlightColor) {
        this.highlightColor = highlightColor;
        return this;
    }

    public OmniBuilder<T> setSelectionColor(@ColorInt int selectionColor) {
        this.selectionColor = selectionColor;
        return this;
    }

    public OmniBuilder<T> setSelectionMode(@NonNull SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
        return this;
    }

    public OmniBuilder<T> setExpandUntilLevelOnStartup(@IntRange(from = 0) int expandUntilLevelOnStartup) {
        this.expandUntilLevelOnStartup = expandUntilLevelOnStartup;
        return this;
    }

    public OmniBuilder<T> setDeselectChildrenOnCollapse(boolean deselectChildrenOnCollapse) {
        this.deselectChildrenOnCollapse = deselectChildrenOnCollapse;
        return this;
    }

    public OmniBuilder<T> addSelectionListener(OmniAdapter.SelectionListener<T> listener) {
        this.selectionListeners.add(listener);
        return this;
    }

    public OmniBuilder<T> enableUndoForAction(@Action.UndoableAction int action, @StringRes int textId) {
        return enableUndoForAction(action, context.getString(textId));
    }

    public OmniBuilder<T> enableUndoForAction(@Action.UndoableAction int action, String text) {
        enabledUndoActions.put(action, text);
        return this;
    }

    public OmniBuilder<T> setUndoButtonText(@StringRes int undoTextId) {
        return setUndoButtonText(context.getString(undoTextId));
    }

    public OmniBuilder<T> setUndoButtonText(@NonNull String undoText) {
        this.undoText = undoText;
        return this;
    }

    public OmniBuilder<T> addUndoListener(OmniAdapter.UndoListener<T> listener) {
        this.undoListeners.add(listener);
        return this;
    }

    public OmniBuilder<T> setInsetDpPerLevel(int insetDpPerLevel) {
        this.insetDpPerLevel = insetDpPerLevel;
        return this;
    }

    public OmniBuilder<T> setInsetAsMargin(boolean insetAsMargin) {
        this.insetAsMargin = insetAsMargin;
        return this;
    }

    public OmniAdapter<T> attach(RecyclerView recyclerView) {
        OmniAdapterImpl<T> adapter = new OmniAdapterImpl<>(context, dataSource, controller,
                click, longClick, swipeToLeft, swipeToRight, layoutManager,
                highlightColor, selectionColor, selectionMode, expandUntilLevelOnStartup,
                deselectChildrenOnCollapse, selectionListeners,
                enabledUndoActions, undoText, undoListeners, insetDpPerLevel, insetAsMargin);
        recyclerView.setAdapter(adapter);
        adapter.attach(recyclerView);
        return adapter;
    }
}
