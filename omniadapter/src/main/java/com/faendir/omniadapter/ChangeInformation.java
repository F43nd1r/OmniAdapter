package com.faendir.omniadapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created on 14.08.2016.
 *
 * @author F43nd1r
 */
public class ChangeInformation<T extends Component> implements Comparable<ChangeInformation<T>> {

    public enum Type {
        ADD,
        MOVE,
        REMOVE
    }

    private static int counter = 0;

    @NonNull
    private final T component;
    private final int creationIndex;
    @NonNull
    private final Type type;
    private final DeepObservableList<T> formerParent;
    private final int formerPosition;
    private final DeepObservableList<T> newParent;

    private ChangeInformation(int creationIndex, @NonNull Type type, @NonNull T component, @Nullable DeepObservableList<T> formerParent, int formerPosition, @Nullable DeepObservableList<T> newParent) {
        this.creationIndex = creationIndex;
        this.type = type;
        this.component = component;
        this.formerParent = formerParent;
        this.formerPosition = formerPosition;
        this.newParent = newParent;
    }

    static synchronized <T extends Component> ChangeInformation<T> removeInfo(T component, DeepObservableList<T> formerParent, int formerPosition) {
        return new ChangeInformation<>(counter++, Type.REMOVE, component, formerParent, formerPosition, null);
    }

    static synchronized <T extends Component> ChangeInformation<T> addInfo(T component, DeepObservableList<T> newParent) {
        return new ChangeInformation<>(counter++, Type.ADD, component, null, -1, newParent);
    }

    static synchronized <T extends Component> ChangeInformation<T> moveInfo(T component, DeepObservableList<T> formerParent, int formerPosition, DeepObservableList<T> newParent) {
        return new ChangeInformation<>(counter++, Type.MOVE, component, formerParent, formerPosition, newParent);
    }

    @NonNull
    public T getComponent() {
        return component;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    public DeepObservableList<T> getFormerParent() {
        return formerParent;
    }

    public int getFormerPosition() {
        return formerPosition;
    }

    public DeepObservableList<T> getNewParent() {
        return newParent;
    }

    @Override
    public int compareTo(@NonNull ChangeInformation<T> changeInformation) {
        return creationIndex - changeInformation.creationIndex;
    }
}
