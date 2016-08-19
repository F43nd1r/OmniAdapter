package com.faendir.omniadapter.model;

import android.support.annotation.NonNull;

import com.faendir.omniadapter.DeepObservableList;

import java.util.UUID;

/**
 * Created on 07.08.2016.
 *
 * @author F43nd1r
 */

public class Composite<T extends Component> implements Component {
    private final UUID uuid;
    private final State state;
    private final DeepObservableList<T> children;

    public Composite() {
        uuid = UUID.randomUUID();
        state = new State();
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

    public final DeepObservableList<T> getChildren(){
        return children;
    }

    public static class State extends Component.State{
        private boolean expanded;
        public State(){
            expanded = false;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            boolean old = this.expanded;
            this.expanded = expanded;
            if(old != expanded && getListener() != null && getListener() instanceof Listener){
                ((Listener) getListener()).onExpansionToggled(expanded);
            }
        }
        public interface Listener extends Component.State.Listener{
            void onExpansionToggled(boolean newValue);
        }
    }
}
