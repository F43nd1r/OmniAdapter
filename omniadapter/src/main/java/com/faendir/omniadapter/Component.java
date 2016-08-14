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
        private boolean selected;
        private boolean enabled;
        private transient Listener listener;

        public State() {
            selected = false;
            enabled = true;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            boolean old = this.selected;
            this.selected = selected;
            if (old != selected && listener != null) {
                listener.onSelectionToggled(selected);
            }
        }

        void setListener(Listener listener) {
            this.listener = listener;
        }

        public Listener getListener() {
            return listener;
        }

        void removeListener(Listener listener) {
            if (this.listener.equals(listener)) {
                this.listener = null;
            }
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            boolean old = this.enabled;
            this.enabled = enabled;
            if (old != enabled && listener != null) {
                listener.onEnabledToggled(enabled);
            }
        }

        interface Listener {
            void onSelectionToggled(boolean newValue);
            void onEnabledToggled(boolean newValue);
        }

    }
}
