package com.faendir.omniadapter;

import java.util.UUID;

/**
 * Created on 07.08.2016.
 *
 * @author F43nd1r
 */

public abstract class Leaf implements Component {
    private final UUID uuid;
    private final State state;

    protected Leaf() {
        uuid = UUID.randomUUID();
        state = new State();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Leaf leaf = (Leaf) o;

        return uuid.equals(leaf.uuid);

    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public int getId() {
        return hashCode();
    }

    @Override
    public State getState() {
        return state;
    }
}
