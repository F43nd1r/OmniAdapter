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

class ComponentViewHolder<T extends Component> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, Component.State.Listener {
    private final View view;
    private T component;
    @Nullable
    private Listener<T> listener;
    private int level;

    ComponentViewHolder(View view, int level, @ColorInt int highlightColor, @ColorInt int selectionColor) {
        super(view);
        this.view = view;
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        this.level = level;
        Utils.setDrawable(view, highlightColor, selectionColor);
    }

    public T getComponent() {
        return component;
    }

    public void setComponent(@NonNull T component) {
        this.component = component;
        component.getState().setListener(this);
    }

    public void detach(){
        if(this.component != null){
            this.component.getState().removeListener(this);
        }
    }

    public void setListener(@Nullable Listener<T> listener) {
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

    interface Listener<T extends Component> {
        void onClick(ComponentViewHolder<T> viewHolder);

        boolean onLongClick(ComponentViewHolder<T> viewHolder);

        void onExpansionToggled(ComponentViewHolder<T> viewHolder);

        void onSelectionToggled(ComponentViewHolder<T> viewHolder);
    }

    public View getView() {
        return view;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
