package com.faendir.omniadapter;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.List;

/**
 * Created on 09.08.2016.
 *
 * @author F43nd1r
 */
@SuppressWarnings("unused")
public interface OmniAdapter<T extends Component> {
    void clearSelection();

    @NonNull
    List<? extends T> getSelection();

    @NonNull
    List<? extends T> getSelectionByLevel(@IntRange(from = 0) int level);

    void notifyItemUpdated(T component);

    @SuppressWarnings("unchecked")
    void notifyItemsUpdated(T... components);

    void notifyItemsUpdated(Collection<? extends T> components);

    void notifyDataSetUpdated();

    @NonNull
    List<? extends T> getVisible();

    @NonNull
    List<? extends T> getVisibleByLevel(@IntRange(from = 0) int level);

    @NonNull
    List<? extends T> getVisibleByParent(T parent);

    int getVisibleCount();

    boolean isVisible(T component);

    interface SelectionListener<T extends Component> {
        void onSelectionChanged(List<T> selected);

        void onSelectionCleared();
    }

    interface Controller<T extends Component> {
        View createView(ViewGroup parent, int level);

        void bindView(View view, T component, int level);

        boolean isExpandable(T component);

        boolean shouldMove(T component, DeepObservableList from, int fromPosition, DeepObservableList to, int toPosition);

        boolean isSelectable(T component);

        boolean shouldSwipe(T component, int direction);
    }

    abstract class BaseController<T extends Component> implements Controller<T> {

        @Override
        public boolean isExpandable(T component) {
            return true;
        }

        @Override
        public boolean shouldMove(T component, DeepObservableList from, int fromPosition, DeepObservableList to, int toPosition) {
            return true;
        }

        @Override
        public boolean isSelectable(T component) {
            return true;
        }

        @Override
        public boolean shouldSwipe(T component, int direction) {
            return true;
        }
    }
}
