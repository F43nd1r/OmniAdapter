package com.faendir.omniadapter;

/**
 * Created on 09.08.2016.
 *
 * @author F43nd1r
 */
public abstract class BaseOmniController<T extends Component> implements OmniController<T> {

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
    public boolean shouldSwipe(T component) {
        return true;
    }
}
