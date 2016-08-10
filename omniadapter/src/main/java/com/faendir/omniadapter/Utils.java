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

import java.util.Arrays;
import java.util.List;

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
        view.setBackgroundDrawable(background);
    }

    static int findLevel(List<? extends Component> search, Component component) {
        for (Component c : search) {
            if (c.equals(component)) {
                return 0;
            } else if (c instanceof DeepObservableList) {
                int level = findLevel((DeepObservableList<? extends Component>) c, component);
                if (level != -1) return level + 1;
            }
        }
        return -1;
    }

    static <T extends Component> DeepObservableList<T> findList(List<? extends T> search, T component) {
        for (Component c : search) {
            if (c.equals(component)) {
                //noinspection unchecked
                return (DeepObservableList<T>) search;
            } else if (c instanceof DeepObservableList) {
                //noinspection unchecked
                DeepObservableList<T> list = findList((DeepObservableList<T>) c, component);
                if (!list.isEmpty()) return list;
            }
        }
        return DeepObservableList.emptyList();
    }

    static <T extends Component> void expandUntilLevel(DeepObservableList<T> list, final OmniController<T> controller, final int expandUntilLevel) {
        list.visitDeep(new DeepObservableList.ComponentVisitor<T>() {
            @Override
            public void visit(T component, int level) {
                if (level <= expandUntilLevel && component instanceof Composite && controller.isExpandable(component)) {
                    component.getState().setExpanded(true);
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
}
