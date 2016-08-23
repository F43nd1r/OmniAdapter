package com.faendir.omniadapter;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.faendir.omniadapter.model.ChangeInformation;
import com.faendir.omniadapter.model.Component;
import com.faendir.omniadapter.model.Composite;
import com.faendir.omniadapter.model.DeepObservableList;

import java.util.Collection;
import java.util.List;

/**
 * Created on 09.08.2016.
 *
 * @author F43nd1r
 */
public interface OmniAdapter<T extends Component> {
    void clearSelection();

    @NonNull
    List<T> getSelection();

    @NonNull
    List<T> getSelectionByLevel(@IntRange(from = 0) int level);

    @NonNull
    <E extends T> List<E> getSelectionByType(Class<E> type);

    void notifyItemUpdated(T component);

    void notifyItemsUpdated(T... components);

    void notifyItemsUpdated(Collection<? extends T> components);

    void notifyDataSetUpdated();

    @NonNull
    List<T> getVisible();

    @NonNull
    List<T> getVisibleByLevel(@IntRange(from = 0) int level);

    @NonNull
    List<T> getVisibleByParent(T parent);

    @NonNull
    <E extends T> List<E> getVisibleByType(Class<E> type);

    int getVisibleCount();

    void commitPendingUndoIfAny();

    boolean isVisible(T component);

    interface SelectionListener<T extends Component> {
        void onSelectionChanged(List<T> selected);

        void onSelectionCleared();
    }

    interface Controller<T extends Component> {
        View createView(ViewGroup parent, int level);

        void bindView(View view, T component, int level);

        boolean shouldMove(T component, DeepObservableList from, int fromPosition, DeepObservableList to, int toPosition);

        boolean isSelectable(T component);

        boolean shouldSwipe(T component, int direction);
    }

    interface ExpandableController<T0 extends Composite<? extends T1>, T1 extends Component> extends Controller<T1> {
        boolean isExpandable(T0 component);
    }

    abstract class BaseController<T extends Component> implements Controller<T> {

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

    abstract class BaseExpandableController<T0 extends Composite<? extends T1>, T1 extends Component> extends BaseController<T1> implements ExpandableController<T0, T1> {

        @Override
        public boolean isExpandable(T0 component) {
            return true;
        }
    }

    interface UndoListener<T extends Component> {
        void onActionPersisted(List<? extends ChangeInformation<T>> changes);

        void onActionReverted(List<? extends ChangeInformation<T>> changes);
    }
}
