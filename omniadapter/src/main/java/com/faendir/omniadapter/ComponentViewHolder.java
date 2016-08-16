package com.faendir.omniadapter;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created on 07.08.2016.
 *
 * @author F43nd1r
 */

class ComponentViewHolder<T extends Component> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, Composite.State.Listener {
    private final View view;
    private T component;
    @Nullable
    private Listener<T> listener;
    private final int level;

    ComponentViewHolder(View view, int level, @ColorInt int highlightColor, @ColorInt int selectionColor, int insetDpPerLevel, boolean insetAsMargin) {
        super(view);
        this.view = view;
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        this.level = level;
        Utils.setDrawable(view, highlightColor, selectionColor);
        Utils.applyInset(view, insetDpPerLevel * level, insetAsMargin);
    }

    T getComponent() {
        return component;
    }

    void setComponent(@NonNull T component) {
        this.component = component;
        component.getState().setListener(this);
        view.setEnabled(component.getState().isEnabled());
        view.setActivated(component.getState().isSelected());
    }

    void detach(){
        if(this.component != null){
            this.component.getState().removeListener(this);
        }
    }

    void setListener(@Nullable Listener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (listener != null) listener.onClick(this);
    }

    @Override
    public boolean onLongClick(View view) {
        return listener != null && listener.onLongClick(this);
    }

    @Override
    public void onSelectionToggled(boolean newValue) {
        getView().setActivated(newValue);
        if(listener!= null){
            listener.onSelectionToggled(this);
        }
    }

    @Override
    public void onExpansionToggled(boolean newValue) {
        if(listener!= null){
            listener.onExpansionToggled(this);
        }
    }

    @Override
    public void onEnabledToggled(boolean newValue) {
        getView().setEnabled(newValue);
    }

    interface Listener<T extends Component> {
        void onClick(ComponentViewHolder<T> viewHolder);

        boolean onLongClick(ComponentViewHolder<T> viewHolder);

        void onExpansionToggled(ComponentViewHolder<T> viewHolder);

        void onSelectionToggled(ComponentViewHolder<T> viewHolder);
    }

    View getView() {
        return view;
    }

    int getLevel() {
        return level;
    }
}
