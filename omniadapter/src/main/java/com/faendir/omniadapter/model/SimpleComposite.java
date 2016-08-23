package com.faendir.omniadapter.model;

import android.support.annotation.NonNull;

import java.util.UUID;

/**
 * Created on 07.08.2016.
 *
 * @author F43nd1r
 */

public class SimpleComposite<T extends Component> implements Composite<T> {
    private final UUID uuid;
    private final ExpandableState state;
    private final DeepObservableList<T> children;

    public SimpleComposite() {
        uuid = UUID.randomUUID();
        state = new ExpandableState();
        children = new DeepObservableList<>();
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SimpleComposite that = (SimpleComposite) o;

        return uuid.equals(that.uuid);
    }

    @Override
    public int getId() {
        return hashCode();
    }

    @NonNull
    @Override
    public ExpandableState getState() {
        return state;
    }

    @Override
    public final DeepObservableList<T> getChildren(){
        return children;
    }

}
