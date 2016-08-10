package com.faendir.omniadapter;

import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.Range;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 09.08.2016.
 *
 * @author F43nd1r
 */

public class Action {
    static final int NONE = -1;
    public static final int SELECT = 1;
    public static final int EXPAND = 2;
    public static final int DRAG = 3;
    public static final int REMOVE = 4;
    public static final int CUSTOM = 10;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, SELECT, REMOVE, CUSTOM})
    public @interface GenericAction {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, SELECT, DRAG, REMOVE, CUSTOM})
    public @interface LongClickAction {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, SELECT, EXPAND, REMOVE, CUSTOM})
    public @interface CompositeAction {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, SELECT, EXPAND, DRAG, REMOVE, CUSTOM})
    public @interface LongClickCompositeAction {
    }

    static class BaseAction {
        @NonNull
        private final Listener listener;
        private final int defaultAction;
        private int defaultCompositeAction;
        private Map<Range<Integer>, Integer> actions;
        private Map<Range<Integer>, Integer> compositeActions;

        public BaseAction(int defaultAction, @NonNull Listener listener) {
            this.listener = listener;
            this.defaultAction = defaultAction;
            this.defaultCompositeAction = NONE;
            actions = new HashMap<>();
            compositeActions = new HashMap<>();
        }

        public BaseAction setDefaultCompositeAction(int defaultCompositeAction) {
            this.defaultCompositeAction = defaultCompositeAction;
            return this;
        }

        public BaseAction setAction(int action, @IntRange(from = 0) int level) {
            return setAction(action, level, level);
        }

        public BaseAction setAction(int action, @IntRange(from = 0) int fromLevel, @IntRange(from = 0) int toLevel) {
            actions.put(Range.between(fromLevel, toLevel), action);
            return this;
        }

        public BaseAction setCompositeAction(int action, @IntRange(from = 0) int level) {
            return setCompositeAction(action, level, level);
        }

        public BaseAction setCompositeAction(int action, @IntRange(from = 0) int fromLevel, @IntRange(from = 0) int toLevel) {
            compositeActions.put(Range.between(fromLevel, toLevel), action);
            return this;
        }

        int resolve(Component component, int level) {
            if (component instanceof Composite) {
                for (Map.Entry<Range<Integer>, Integer> entry : compositeActions.entrySet()) {
                    if (entry.getKey().contains(level)) {
                        return entry.getValue();
                    }
                }
                if (defaultCompositeAction != NONE) {
                    return defaultCompositeAction;
                }
            }
            for (Map.Entry<Range<Integer>, Integer> entry : actions.entrySet()) {
                if (entry.getKey().contains(level)) {
                    return entry.getValue();
                }
            }
            return defaultAction;
        }

        @NonNull
        Listener getListener() {
            return listener;
        }

        public interface Listener {
            boolean allowTrigger(Component component, int action);

            void onTrigger(Component component, int action);
        }

    }


    public static class Click extends BaseAction {

        public Click(@GenericAction int defaultAction) {
            this(defaultAction, null);
        }

        public Click(@GenericAction int defaultAction, @Nullable final Listener listener) {
            super(defaultAction, new BaseAction.Listener() {
                @Override
                public boolean allowTrigger(Component component, int action) {
                    return listener == null || listener.allowClick(component, action);
                }

                @Override
                public void onTrigger(Component component, int action) {
                    if (listener != null) {
                        listener.onClick(component, action);
                    }
                }
            });
        }

        @Override
        public Click setDefaultCompositeAction(@CompositeAction int defaultCompositeAction) {
            return (Click) super.setDefaultCompositeAction(defaultCompositeAction);
        }

        @Override
        public Click setAction(@GenericAction int action, @IntRange(from = 0) int level) {
            return (Click) super.setAction(action, level);
        }

        @Override
        public Click setAction(@GenericAction int action, @IntRange(from = 0) int fromLevel, @IntRange(from = 0) int toLevel) {
            return (Click) super.setAction(action, fromLevel, toLevel);
        }

        @Override
        public Click setCompositeAction(@CompositeAction int action, @IntRange(from = 0) int level) {
            return (Click) super.setCompositeAction(action, level);
        }

        @Override
        public Click setCompositeAction(@CompositeAction int action, @IntRange(from = 0) int fromLevel, @IntRange(from = 0) int toLevel) {
            return (Click) super.setCompositeAction(action, fromLevel, toLevel);
        }

        public interface Listener {
            boolean allowClick(Component component, int action);

            void onClick(Component component, int action);
        }
    }

    public static class LongClick extends BaseAction {

        public LongClick(@LongClickAction int defaultAction) {
            this(defaultAction, null);
        }

        public LongClick(@LongClickAction int defaultAction, @Nullable final LongClick.Listener listener) {
            super(defaultAction, new BaseAction.Listener() {
                @Override
                public boolean allowTrigger(Component component, int action) {
                    return listener == null || listener.allowLongClick(component, action);
                }

                @Override
                public void onTrigger(Component component, int action) {
                    if (listener != null) {
                        listener.onLongClick(component, action);
                    }
                }
            });
        }

        @Override
        public LongClick setDefaultCompositeAction(@LongClickCompositeAction int defaultCompositeAction) {
            return (LongClick) super.setDefaultCompositeAction(defaultCompositeAction);
        }

        @Override
        public LongClick setAction(@LongClickAction int action, @IntRange(from = 0) int level) {
            return (LongClick) super.setAction(action, level);
        }

        @Override
        public LongClick setAction(@LongClickAction int action, @IntRange(from = 0) int fromLevel, @IntRange(from = 0) int toLevel) {
            return (LongClick) super.setAction(action, fromLevel, toLevel);
        }

        @Override
        public LongClick setCompositeAction(@LongClickCompositeAction int action, @IntRange(from = 0) int level) {
            return (LongClick) super.setCompositeAction(action, level);
        }

        @Override
        public LongClick setCompositeAction(@LongClickCompositeAction int action, @IntRange(from = 0) int fromLevel, @IntRange(from = 0) int toLevel) {
            return (LongClick) super.setCompositeAction(action, fromLevel, toLevel);
        }

        public interface Listener {
            boolean allowLongClick(Component component, int action);

            void onLongClick(Component component, int action);
        }
    }

    public static class Swipe extends BaseAction {

        public Swipe(@GenericAction int defaultAction) {
            this(defaultAction, null);
        }

        public Swipe(@GenericAction int defaultAction, @Nullable final Listener listener) {
            super(defaultAction, new BaseAction.Listener() {
                @Override
                public boolean allowTrigger(Component component, int action) {
                    return listener == null || listener.allowSwipe(component, action);
                }

                @Override
                public void onTrigger(Component component, int action) {
                    if (listener != null) {
                        listener.onSwipe(component, action);
                    }
                }
            });
        }

        @Override
        public Swipe setDefaultCompositeAction(@CompositeAction int defaultCompositeAction) {
            return (Swipe) super.setDefaultCompositeAction(defaultCompositeAction);
        }

        @Override
        public Swipe setAction(@GenericAction int action, @IntRange(from = 0) int level) {
            return (Swipe) super.setAction(action, level);
        }

        @Override
        public Swipe setAction(@GenericAction int action, @IntRange(from = 0) int fromLevel, @IntRange(from = 0) int toLevel) {
            return (Swipe) super.setAction(action, fromLevel, toLevel);
        }

        @Override
        public Swipe setCompositeAction(@CompositeAction int action, @IntRange(from = 0) int level) {
            return (Swipe) super.setCompositeAction(action, level);
        }

        @Override
        public Swipe setCompositeAction(@CompositeAction int action, @IntRange(from = 0) int fromLevel, @IntRange(from = 0) int toLevel) {
            return (Swipe) super.setCompositeAction(action, fromLevel, toLevel);
        }

        public interface Listener {
            boolean allowSwipe(Component component, int action);

            void onSwipe(Component component, int action);
        }
    }

}
