package com.faendir.omniadapter;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.view.View;

import org.apache.commons.lang3.event.EventListenerSupport;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 09.08.2016.
 *
 * @author F43nd1r
 */

final class Utils {
    static void setDrawable(View view, @ColorInt int highlightColor, @ColorInt int selectionColor) {
        Drawable background;
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_activated}, new ColorDrawable(selectionColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float[] outerRadii = new float[8];
            Arrays.fill(outerRadii, 3);
            RoundRectShape r = new RoundRectShape(outerRadii, null, null);
            ShapeDrawable shapeDrawable = new ShapeDrawable(r);
            shapeDrawable.getPaint().setColor(selectionColor);
            background = new RippleDrawable(ColorStateList.valueOf(highlightColor), drawable, shapeDrawable);
        } else {
            drawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(highlightColor));
            int duration = view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
            drawable.setEnterFadeDuration(duration);
            drawable.setExitFadeDuration(duration);
            background = drawable;
        }
        drawable.addState(new int[0], view.getBackground());
        //noinspection deprecation
        view.setBackgroundDrawable(background);
    }

    static int findLevel(DeepObservableList<? extends Component> search, Component component) {
        for (Component c : search) {
            if (c.equals(component)) {
                return 0;
            } else if (c instanceof Composite) {
                int level = findLevel(((Composite<? extends Component>) c).getChildren(), component);
                if (level != -1) return level + 1;
            }
        }
        return -1;
    }

    static <T extends Component> DeepObservableList<T> findParent(DeepObservableList<? extends T> search, T component) {
        for (Component c : search) {
            if (c.equals(component)) {
                //noinspection unchecked
                return (DeepObservableList<T>) search;
            } else if (c instanceof Composite) {
                //noinspection unchecked
                DeepObservableList<T> list = findParent(((Composite<T>) c).getChildren(), component);
                if (!list.isEmpty()) return list;
            }
        }
        return new DeepObservableList<>();
    }

    static <T extends Component> void expandUntilLevel(DeepObservableList<T> list, final OmniAdapter.Controller<T> controller, final int expandUntilLevel) {
        list.visitDeep(new DeepObservableList.ComponentVisitor<T>() {
            @Override
            public void visit(T component, int level) {
                if (level <= expandUntilLevel && component instanceof Composite && controller.isExpandable(component)) {
                    ((Composite)component).getState().setExpanded(true);
                }
            }
        });
    }


    static <T extends Component> void clearSelection(DeepObservableList<T> list) {
        list.visitDeep(new DeepObservableList.ComponentVisitor<T>() {
            @Override
            public void visit(T component, int level) {
                component.getState().setSelected(false);
            }
        });
    }


    static <T> EventListenerSupport<T> createGenericEventListenerSupport(Class<? super T> listener) {
        //noinspection unchecked
        return (EventListenerSupport<T>) new EventListenerSupport<>(listener);
    }

    static <T> EventListenerSupport<T> createGenericEventListenerSupport(Class<? super T> listener, List<? extends T> listeners) {
        EventListenerSupport<T> support = createGenericEventListenerSupport(listener);
        for (T l : listeners) {
            support.addListener(l);
        }
        return support;
    }

    static <T extends Component> Collection<ChangeInformation<T>> compileChanges(List<ChangeInformation<T>> changes) {
        Collections.sort(changes);
        Map<T, ChangeInformation<T>> map = new HashMap<>();
        for (ChangeInformation<T> change : changes) {
            T component = change.getComponent();
            if (map.containsKey(component)) {
                ChangeInformation<T> prevChange = map.get(component);
                switch (change.getType()) {
                    case REMOVE:
                        if (prevChange.getType() == ChangeInformation.Type.ADD) {
                            map.remove(component);
                        } else {
                            map.put(component, ChangeInformation.removeInfo(component, prevChange.getFormerParent(), prevChange.getFormerPosition()));
                        }
                        break;
                    case ADD:
                    case MOVE:
                        if (prevChange.getType() == ChangeInformation.Type.ADD) {
                            map.put(component, ChangeInformation.addInfo(component, change.getNewParent()));
                        } else {
                            map.put(component, ChangeInformation.moveInfo(component, prevChange.getFormerParent(), prevChange.getFormerPosition(), change.getNewParent()));
                        }
                        break;
                }
            } else {
                map.put(component, change);
            }
        }
        return map.values();
    }
}
