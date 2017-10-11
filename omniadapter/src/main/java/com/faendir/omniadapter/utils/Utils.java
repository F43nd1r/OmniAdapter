package com.faendir.omniadapter.utils;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;

import com.faendir.omniadapter.OmniAdapter;
import com.faendir.omniadapter.model.ChangeInformation;
import com.faendir.omniadapter.model.Component;
import com.faendir.omniadapter.model.Composite;
import com.faendir.omniadapter.model.DeepObservableList;

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

public final class Utils {
    public static void setDrawable(View view, @ColorInt int highlightColor, @ColorInt int selectionColor) {
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

    public static void applyInset(View view, int insetDp, boolean asMargin) {
        DisplayMetrics displayMetrics = view.getContext().getResources().getDisplayMetrics();
        int inset = Math.round(insetDp * displayMetrics.density);
        if (asMargin) {
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
            layoutParams.leftMargin = layoutParams.leftMargin + inset;
            view.setLayoutParams(layoutParams);
        } else {
            view.setPadding(view.getPaddingLeft() + inset, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    public static int findLevel(DeepObservableList<? extends Component> search, Component component) {
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

    public static <T extends Component> DeepObservableList<T> findParent(DeepObservableList<? extends T> search, T component) {
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
        //noinspection unchecked
        return new DeepObservableList<T>((Class<T>) component.getClass());
    }

    public static <T extends Component> void expandUntilLevel(DeepObservableList<T> list, final OmniAdapter.Controller<T> controller, final int expandUntilLevel) {
        list.visitDeep(new DeepObservableList.ComponentVisitor<T>() {
            @Override
            public void visit(T component, int level) {
                if (level <= expandUntilLevel && checkIsExpandable(controller, component)) {
                    ((Composite) component).getState().setExpanded(true);
                }
            }
        }, false);
    }


    public static <T extends Component> void clearSelection(DeepObservableList<T> list) {
        list.visitDeep(new DeepObservableList.ComponentVisitor<T>() {
            @Override
            public void visit(T component, int level) {
                component.getState().setSelected(false);
            }
        }, false);
    }


    @NonNull
    public static <T> EventListenerSupport<T> createGenericEventListenerSupport(Class<? super T> listener) {
        //noinspection unchecked (this can't already contain elements of a different type)
        return (EventListenerSupport<T>) new EventListenerSupport<>(listener);
    }

    @NonNull
    public static <T> EventListenerSupport<T> createGenericEventListenerSupport(Class<? super T> listener, List<? extends T> listeners) {
        EventListenerSupport<T> support = createGenericEventListenerSupport(listener);
        for (T l : listeners) {
            support.addListener(l);
        }
        return support;
    }

    @NonNull
    public static <T extends Component> Collection<ChangeInformation<T>> compileChanges(List<ChangeInformation<T>> changes) {
        Collections.sort(changes);
        Map<T, ChangeInformation<T>> map = new HashMap<>();
        for (ChangeInformation<T> change : changes) {
            T component = change.getComponent();
            if (map.containsKey(component)) {
                ChangeInformation<T> prevChange = map.get(component);
                if(change instanceof ChangeInformation.Remove){
                    if (prevChange instanceof ChangeInformation.IRemove) {
                        //noinspection unchecked (type of change always equals the type of the interface)
                        ChangeInformation.IRemove<T> remove = (ChangeInformation.IRemove<T>) prevChange;
                        map.put(component, new ChangeInformation.Remove<>(component, remove.getFormerParent(), remove.getFormerPosition()));
                    } else {
                        map.remove(component);
                    }
                }else if(change instanceof ChangeInformation.IAdd){
                    //noinspection unchecked (type of change always equals the type of the interface)
                    ChangeInformation.IAdd<T> iAdd = (ChangeInformation.IAdd<T>) change;
                    if (prevChange instanceof ChangeInformation.IRemove) {
                        //noinspection unchecked (type of change always equals the type of the interface)
                        ChangeInformation.IRemove<T> remove = (ChangeInformation.IRemove<T>) prevChange;
                        map.put(component, new ChangeInformation.Move<>(component, remove.getFormerParent(), remove.getFormerPosition(), iAdd.getNewParent()));
                    }else if (prevChange instanceof ChangeInformation.Add){
                        map.put(component, new ChangeInformation.Add<>(component, iAdd.getNewParent()));
                    }
                }
            } else {
                map.put(component, change);
            }
        }
        return map.values();
    }

    public static <T extends Component> boolean checkIsExpandable(OmniAdapter.Controller<T> controller, T component){
        //noinspection unchecked
        return component instanceof Composite
                && controller instanceof OmniAdapter.ExpandableController
                && ((OmniAdapter.ExpandableController<Composite<? extends T>, T>) controller).isExpandable((Composite<? extends T>) component);
    }
}
