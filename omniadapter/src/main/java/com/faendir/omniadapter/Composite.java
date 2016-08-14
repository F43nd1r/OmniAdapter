package com.faendir.omniadapter;

import android.support.annotation.NonNull;

import java.util.UUID;

/**
 * Created on 07.08.2016.
 *
 * @author F43nd1r
 */

public class Composite<T extends Component> extends DeepObservableList<T> implements Component {
    private final UUID uuid;
    private final State state;

    public Composite() {
        uuid = UUID.randomUUID();
        state = new State();
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

        Composite that = (Composite) o;

        return uuid.equals(that.uuid);
    }

    @Override
    public int getId() {
        return hashCode();
    }

    @NonNull
    @Override
    public State getState() {
        return state;
    }
}
