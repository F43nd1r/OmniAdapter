package com.faendir.omniadapter;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created on 08.08.2016.
 *
 * @author F43nd1r
 */

public interface OmniController<T extends Component> {
    View createView(ViewGroup parent, int level);

    void bindView(View view, T component, int level);

    boolean isExpandable(T component);

    boolean shouldMove(T component, DeepObservableList from, int fromPosition, DeepObservableList to, int toPosition);

    boolean isSelectable(T component);

    boolean shouldSwipe(T component);
}
