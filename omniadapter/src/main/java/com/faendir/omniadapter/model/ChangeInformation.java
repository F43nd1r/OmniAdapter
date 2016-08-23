package com.faendir.omniadapter.model;

import android.support.annotation.NonNull;

/**
 * Created on 14.08.2016.
 *
 * @author F43nd1r
 */
public abstract class ChangeInformation<T extends Component> implements Comparable<ChangeInformation<T>> {
    private static int counter = 0;

    @NonNull
    private final T component;
    private final int creationIndex;

    private ChangeInformation(@NonNull T component) {
        synchronized (ChangeInformation.class) {
            this.creationIndex = counter++;
        }
        this.component = component;
    }

    @NonNull
    public T getComponent() {
        return component;
    }

    @Override
    public int compareTo(@NonNull ChangeInformation<T> changeInformation) {
        return creationIndex - changeInformation.creationIndex;
    }

    public interface IAdd<T extends Component> {
        DeepObservableList<T> getNewParent();
    }

    public interface IRemove<T extends Component> {
        DeepObservableList<T> getFormerParent();

        int getFormerPosition();
    }

    public final static class Remove<T extends Component> extends ChangeInformation<T> implements IRemove<T> {
        @NonNull
        private final DeepObservableList<T> formerParent;
        private final int formerPosition;

        public Remove(@NonNull T component, @NonNull DeepObservableList<T> formerParent, int formerPosition) {
            super(component);
            this.formerParent = formerParent;
            this.formerPosition = formerPosition;
        }

        @NonNull
        public DeepObservableList<T> getFormerParent() {
            return formerParent;
        }

        public int getFormerPosition() {
            return formerPosition;
        }
    }

    public final static class Add<T extends Component> extends ChangeInformation<T> implements IAdd<T>{

        @NonNull
        private final DeepObservableList<T> newParent;

        public Add(@NonNull T component, @NonNull DeepObservableList<T> newParent) {
            super(component);
            this.newParent = newParent;
        }

        @NonNull
        public DeepObservableList<T> getNewParent() {
            return newParent;
        }
    }

    public final static class Move<T extends Component> extends ChangeInformation<T> implements IAdd<T>, IRemove<T>{
        private final IAdd<T> add;
        private final IRemove<T> remove;

        public Move(@NonNull T component, @NonNull DeepObservableList<T> formerParent, int formerPosition, @NonNull DeepObservableList<T> newParent) {
            super(component);
            add = new Add<>(component, newParent);
            remove = new Remove<>(component, formerParent, formerPosition);
        }

        @Override
        public DeepObservableList<T> getNewParent() {
            return add.getNewParent();
        }

        @Override
        public DeepObservableList<T> getFormerParent() {
            return remove.getFormerParent();
        }

        @Override
        public int getFormerPosition() {
            return remove.getFormerPosition();
        }
    }
}
