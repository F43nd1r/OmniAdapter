package com.faendir.omniadapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.apache.commons.lang3.builder.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 09.08.2016.
 *
 * @author F43nd1r
 */

@SuppressWarnings("unused")
public class OmniBuilder<T extends Component> implements Builder<OmniAdapter> {
    @NonNull
    private final Context context;
    @NonNull
    private final DeepObservableList<? extends T> dataSource;
    @NonNull
    private final OmniController<T> controller;
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
    private List<SelectionListener<T>> selectionListeners;

    public OmniBuilder(@NonNull Context context, @NonNull DeepObservableList<? extends T> dataSource, @NonNull OmniController<T> controller) {
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

    public OmniBuilder<T> addSelectionListener(SelectionListener<T> listener){
        this.selectionListeners.add(listener);
        return this;
    }

    @Override
    public OmniAdapter<T> build() {
        return new OmniAdapterImpl<>(context, dataSource, controller,
                click, longClick, swipeToLeft, swipeToRight, layoutManager,
                highlightColor, selectionColor, selectionMode, expandUntilLevelOnStartup,
                deselectChildrenOnCollapse, selectionListeners);
    }
}