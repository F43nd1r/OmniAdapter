package com.faendir.omniadapter;

import android.support.annotation.NonNull;

/**
 * Created on 07.08.2016.
 *
 * @author F43nd1r
 */

public interface Component {
    int getId();

    @NonNull
    State getState();

    class State {
        private boolean isExpanded;
        private boolean isSelected;
        private transient Listener listener;

        public boolean isExpanded() {
            return isExpanded;
        }

        public void setExpanded(boolean expanded) {
            if(isExpanded != expanded && listener != null){
                listener.onExpansionToggled(expanded);
            }
            isExpanded = expanded;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            if (isSelected != selected && listener != null) {
                listener.onSelectionToggled(selected);
            }
            isSelected = selected;
        }

        void setListener(Listener listener) {
            this.listener = listener;
        }

        void removeListener(Listener listener) {
            if (this.listener.equals(listener)) {
                this.listener = null;
            }
        }

        interface Listener {
            void onSelectionToggled(boolean newValue);
            void onExpansionToggled(boolean newValue);
        }

    }
}
