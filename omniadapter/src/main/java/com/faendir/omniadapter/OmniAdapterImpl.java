package com.faendir.omniadapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created on 07.08.2016.
 *
 * @author F43nd1r
 */

class OmniAdapterImpl<T extends Component> extends OmniAdapter<T> implements ComponentViewHolder.Listener<T>, DeepObservableList.Listener {
    private final DeepObservableList<T> basis;
    private final OmniController<T> controller;
    private final ItemTouchHelper touchHelper;
    private final Action.Click click;
    private final Action.LongClick longClick;
    private final Action.Swipe swipeToLeft;
    private final Action.Swipe swipeToRight;
    private final RecyclerView.LayoutManager layoutManager;
    @ColorInt
    private final int highlightColor;
    @ColorInt
    private final int selectionColor;
    private final Handler mainHandler;
    private final SelectionMode selectionMode;
    private final boolean deselectChildrenOnCollapse;
    private final List<SelectionListener<T>> selectionListeners;
    private List<T> visible;
    private boolean bufferedUpdate;

    OmniAdapterImpl(Context context, @NonNull DeepObservableList<? extends T> basis, OmniController<T> controller,
                    Action.Click click, Action.LongClick longClick, Action.Swipe swipeToLeft, Action.Swipe swipeToRight,
                    RecyclerView.LayoutManager layoutManager,
                    int highlightColor, int selectionColor, SelectionMode selectionMode,
                    int expandUntilLevelOnStartup, boolean deselectChildrenOnCollapse,
                    List<SelectionListener<T>> selectionListeners) {
        this.click = click;
        this.longClick = longClick;
        this.swipeToLeft = swipeToLeft;
        this.swipeToRight = swipeToRight;
        this.layoutManager = layoutManager;
        this.highlightColor = highlightColor;
        this.selectionColor = selectionColor;
        this.selectionMode = selectionMode;
        this.deselectChildrenOnCollapse = deselectChildrenOnCollapse;
        this.selectionListeners = selectionListeners;
        setHasStableIds(true);
        bufferedUpdate = false;
        //noinspection unchecked
        this.basis = (DeepObservableList<T>) basis;
        this.controller = controller;
        mainHandler = new Handler(context.getMainLooper());
        visible = this.basis.flatView();
        touchHelper = new ItemTouchHelper(new TouchCallback());
        Utils.expandUntilLevel(this.basis, controller, expandUntilLevelOnStartup);
        basis.addListener(this);
    }

    @Override
    public ComponentViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        ComponentViewHolder<T> componentViewHolder = new ComponentViewHolder<>(controller.createView(parent, viewType), viewType, highlightColor, selectionColor);
        componentViewHolder.setListener(this);
        return componentViewHolder;
    }

    @Override
    public void onBindViewHolder(ComponentViewHolder<T> holder, int position) {
        T component = visible.get(position);
        controller.bindView(holder.getView(), component, holder.getLevel());
        holder.setComponent(visible.get(position));
    }

    @Override
    public void onViewRecycled(ComponentViewHolder<T> holder) {
        holder.detach();
    }

    @Override
    public int getItemCount() {
        return visible.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Utils.findLevel(basis, visible.get(position));
    }

    @Override
    public long getItemId(int position) {
        return visible.get(position).getId();
    }

    @Override
    public void onClick(ComponentViewHolder<T> viewHolder) {
        executeAction(click, viewHolder);
    }

    @Override
    public boolean onLongClick(ComponentViewHolder<T> viewHolder) {
        return executeAction(longClick, viewHolder);
    }

    @Override
    public void onExpansionToggled(ComponentViewHolder<T> viewHolder) {
        update();
    }

    @Override
    public void onSelectionToggled(ComponentViewHolder<T> viewHolder) {
        List<T> selection = getSelection();
        if (selection.size() > 0) {
            for (SelectionListener<T> selectionListener : selectionListeners) {
                selectionListener.onSelectionChanged((List<T>) selection);
            }
        } else {
            for (SelectionListener<T> selectionListener : selectionListeners) {
                selectionListener.onSelectionCleared();
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void clearSelection() {
        Utils.clearSelection(basis);
    }

    @Override
    public List<T> getSelection() {
        final List<T> list = new ArrayList<>();
        basis.visitDeep(new DeepObservableList.ComponentVisitor<T>() {
            @Override
            public void visit(T component, int level) {
                if (component.getState().isSelected()) {
                    list.add(component);
                }
            }
        });
        return list;
    }

    @Override
    public List<T> getSelectionByLevel(@IntRange(from = 0) final int l) {
        final List<T> list = new ArrayList<>();
        basis.visitDeep(new DeepObservableList.ComponentVisitor<T>() {
            @Override
            public void visit(T component, int level) {
                if (l == level && component.getState().isSelected()) {
                    list.add(component);
                }
            }
        });
        return list;
    }

    private boolean executeAction(final Action.BaseAction action, final ComponentViewHolder<T> viewHolder) {
        final T component = viewHolder.getComponent();
        final int actionId = action.resolve(component, viewHolder.getLevel());
        if (actionId == Action.NONE) {
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!action.getListener().allowTrigger(component, actionId)) {
                    return;
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean actionSuccess = true;
                        switch (actionId) {
                            case Action.SELECT:
                                actionSuccess = toggleSelection(viewHolder.getComponent());
                                break;
                            case Action.EXPAND:
                                actionSuccess = toggleExpansion(viewHolder.getComponent());
                                break;
                            case Action.REMOVE:
                                Utils.findList(basis, component).remove(component);
                                update();
                                break;
                            case Action.DRAG:
                                actionSuccess = false;
                                break;
                        }
                        if (actionSuccess) {
                            action.getListener().onTrigger(component, actionId);
                        }
                    }
                });
            }
        }).start();
        return true;
    }

    private void update() {
        if (!bufferedUpdate) {
            bufferedUpdate = true;
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    bufferedUpdate = false;
                    final List<T> old = visible;
                    List<T> current = basis.flatView();
                    Iterator<T> iterator = current.iterator();
                    List<T> possiblyRemoved = new ArrayList<>();
                    List<T> possibleAdded = new ArrayList<>();
                    List<T> possibleMoved = new ArrayList<>();
                    for (T oldComponent : old) {
                        T currentComponent = iterator.hasNext() ? iterator.next() : null;
                        if (oldComponent != currentComponent) {
                            if (possibleAdded.remove(oldComponent)) {
                                possibleMoved.add(oldComponent);
                            } else {
                                possiblyRemoved.add(oldComponent);
                            }
                            if (currentComponent != null) {
                                if (possiblyRemoved.remove(currentComponent)) {
                                    possibleMoved.add(currentComponent);
                                } else {
                                    possibleAdded.add(currentComponent);
                                }
                            }
                        }
                    }
                    while (iterator.hasNext()) {
                        possibleAdded.add(iterator.next());
                    }
                    visible = current;
                    for (T component : possiblyRemoved) {
                        int index = old.indexOf(component);
                        notifyItemRemoved(index);
                        old.remove(index);
                    }
                    for (T component : possibleAdded) {
                        int index = current.indexOf(component);
                        notifyItemInserted(index);
                        old.add(index, component);
                    }
                    for (T component : possibleMoved) {
                        int from = old.indexOf(component);
                        int to = current.indexOf(component);
                        if (from != to) {
                            notifyItemMoved(from, to);
                            old.add(to, old.remove(from));
                        }
                    }
                }
            });
        }
    }

    private boolean toggleSelection(T component) {
        if (controller.isSelectable(component)) {
            boolean select = !component.getState().isSelected();
            if (select && selectionMode == SelectionMode.SINGLE) {
                clearSelection();
            }
            component.getState().setSelected(select);
            return true;
        }
        return false;
    }

    private boolean toggleExpansion(T component) {
        if (component instanceof Composite && !((Composite) component).isEmpty() && controller.isExpandable(component)) {
            component.getState().setExpanded(!component.getState().isExpanded());
            if (deselectChildrenOnCollapse) {
                //noinspection unchecked
                Utils.clearSelection((DeepObservableList<Component>) component);
            }
            update();
            return true;
        }
        return false;
    }

    @Override
    public void onListChanged() {
        update();
    }

    private class TouchCallback extends ItemTouchHelper.SimpleCallback {
        TouchCallback() {
            super(0, 0);
        }

        @Override
        public int getDragDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            ComponentViewHolder holder = (ComponentViewHolder) viewHolder;
            return longClick.resolve(holder.getComponent(), holder.getLevel()) == Action.DRAG ? ItemTouchHelper.UP | ItemTouchHelper.DOWN : 0;
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            ComponentViewHolder holder = (ComponentViewHolder) viewHolder;
            return (swipeToLeft.resolve(holder.getComponent(), holder.getLevel()) != Action.NONE ? ItemTouchHelper.LEFT : 0)
                    | (swipeToRight.resolve(holder.getComponent(), holder.getLevel()) != Action.NONE ? ItemTouchHelper.RIGHT : 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //noinspection unchecked
            ComponentViewHolder<T> fromHolder = (ComponentViewHolder<T>) viewHolder;
            //noinspection unchecked
            ComponentViewHolder<T> toHolder = (ComponentViewHolder<T>) target;
            T from = fromHolder.getComponent();
            T to = toHolder.getComponent();
            DeepObservableList<T> toList;
            if (fromHolder.getLevel() == toHolder.getLevel()) {
                toList = Utils.findList(basis, to);
            } else if (fromHolder.getLevel() == toHolder.getLevel() + 1 && to instanceof Composite) {
                //noinspection unchecked
                toList = (DeepObservableList) to;
            } else {
                return false;
            }
            DeepObservableList fromList = Utils.findList(basis, from);
            assert toList != null && fromList != null;
            if (!controller.shouldMove(from, fromList, fromList.indexOf(from), toList, toList.indexOf(to))) {
                return false;
            }
            fromList.remove(from);
            toList.add(toList.equals(to) ? toList.size() : toList.indexOf(to), from);
            update();
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            //noinspection unchecked
            executeAction(direction == ItemTouchHelper.LEFT ? swipeToLeft : swipeToRight, (ComponentViewHolder<T>) viewHolder);
        }
    }

}
