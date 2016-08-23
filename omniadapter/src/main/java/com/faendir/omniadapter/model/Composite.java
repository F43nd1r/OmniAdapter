package com.faendir.omniadapter.model;

import android.support.annotation.NonNull;

/**
 * @author F43nd1r
 * @since 23.08.2016
 */
public interface Composite<T extends Component> extends Component {
    @Override
    int getId();

    @NonNull
    @Override
    ExpandableState getState();

    DeepObservableList<T> getChildren();

    class ExpandableState extends Component.State{
        private boolean expanded;
        public ExpandableState(){
            expanded = false;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            boolean old = this.expanded;
            this.expanded = expanded;
            if(old != expanded && getListener() != null && getListener() instanceof SimpleComposite.ExpandableState.Listener){
                ((SimpleComposite.ExpandableState.Listener) getListener()).onExpansionToggled(expanded);
            }
        }
        public interface Listener extends Component.State.Listener{
            void onExpansionToggled(boolean newValue);
        }
    }
}
